package org.jellyfin.androidtv.ui.playback

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.data.compat.PlaybackException
import org.jellyfin.androidtv.data.compat.StreamInfo
import org.jellyfin.androidtv.data.compat.VideoOptions
import org.jellyfin.androidtv.util.apiclient.Response
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.hlsSegmentApi
import org.jellyfin.sdk.api.client.extensions.mediaInfoApi
import org.jellyfin.sdk.api.client.extensions.videosApi
import org.jellyfin.sdk.model.api.PlayMethod
import org.jellyfin.sdk.model.api.PlaybackInfoDto
import org.jellyfin.sdk.model.api.PlaybackInfoResponse
import org.jellyfin.sdk.model.api.CodecType
import timber.log.Timber

private fun createStreamInfo(
	api: ApiClient,
	options: VideoOptions,
	response: PlaybackInfoResponse,
): StreamInfo = StreamInfo().apply {
	val source = response.mediaSources.firstOrNull {
		options.mediaSourceId != null && it.id == options.mediaSourceId
	} ?: response.mediaSources.firstOrNull()

	itemId = options.itemId
	mediaSource = source
	runTimeTicks = source?.runTimeTicks
	playSessionId = response.playSessionId

	if (source == null) return@apply

	if (options.enableDirectPlay && source.supportsDirectPlay) {
		playMethod = PlayMethod.DIRECT_PLAY
		container = source.container
		mediaUrl = api.videosApi.getVideoStreamUrl(
			itemId = itemId,
			mediaSourceId = source.id,
			static = true,
			tag = source.eTag,
		)
	} else if (options.enableDirectStream && source.supportsDirectStream) {
		playMethod = PlayMethod.DIRECT_STREAM
		container = source.transcodingContainer
		mediaUrl = api.createUrl(requireNotNull(source.transcodingUrl), ignorePathParameters = true)
	} else if (source.supportsTranscoding) {
		playMethod = PlayMethod.TRANSCODE
		container = source.transcodingContainer
		mediaUrl = api.createUrl(requireNotNull(source.transcodingUrl), ignorePathParameters = true)
	}
}

class PlaybackManager(
	private val api: ApiClient
) {
	fun getVideoStreamInfo(
		lifecycleOwner: LifecycleOwner,
		options: VideoOptions,
		startTimeTicks: Long,
		callback: Response<StreamInfo>,
	) = lifecycleOwner.lifecycleScope.launch {
		Timber.d("getVideoStreamInfo: Starting playback for item ${options.itemId}")
		getVideoStreamInfoInternal(options, startTimeTicks, true).fold(
			onSuccess = {
				Timber.d("getVideoStreamInfo: Successfully got stream info")
				callback.onResponse(it)
			},
			onFailure = {
				Timber.e(it, "getVideoStreamInfo: Failed to get stream info")
				callback.onError(Exception(it))
			},
		)
	}

	fun changeVideoStream(
		lifecycleOwner: LifecycleOwner,
		stream: StreamInfo,
		options: VideoOptions,
		startTimeTicks: Long,
		callback: Response<StreamInfo>
	) = lifecycleOwner.lifecycleScope.launch {
		if (stream.playSessionId != null && stream.playMethod != PlayMethod.DIRECT_PLAY) {
			withContext(Dispatchers.IO) {
				api.hlsSegmentApi.stopEncodingProcess(api.deviceInfo.id, stream.playSessionId)
			}
		}

		getVideoStreamInfoInternal(options, startTimeTicks, true).fold(
			onSuccess = { callback.onResponse(it) },
			onFailure = { callback.onError(Exception(it)) },
		)
	}

	private fun isAc3Error(throwable: Throwable?): Boolean {
		if (throwable == null) return false
		val message = throwable.message?.lowercase() ?: ""
		return message.contains("ac3") ||
			   message.contains("eac3") ||
			   message.contains("audio codec not supported")
	}

	private suspend fun getVideoStreamInfoInternal(
		options: VideoOptions,
		startTimeTicks: Long,
		attemptAc3: Boolean = true
	): Result<StreamInfo> {
		try {
			Timber.d("getVideoStreamInfoInternal: Attempting playback (AC3 enabled: $attemptAc3)")
			Timber.d("getVideoStreamInfoInternal: Audio stream index: ${options.audioStreamIndex}")
			Timber.d("getVideoStreamInfoInternal: Media source ID: ${options.mediaSourceId}")
			Timber.d("getVideoStreamInfoInternal: Profile: ${options.profile?.containerProfiles?.joinToString()}")
			val response = withContext(Dispatchers.IO) {
				Timber.d("getVideoStreamInfoInternal: Requesting playback info from server")
				api.mediaInfoApi.getPostedPlaybackInfo(
					itemId = requireNotNull(options.itemId) { "Item id cannot be null" },
					data = PlaybackInfoDto(
						mediaSourceId = options.mediaSourceId,
						startTimeTicks = startTimeTicks,
						deviceProfile = options.profile,
					enableDirectStream = options.enableDirectStream,
					enableDirectPlay = options.enableDirectPlay,
					maxAudioChannels = options.maxAudioChannels,
					audioStreamIndex = options.audioStreamIndex.takeIf { it != null && it >= 0 },
					subtitleStreamIndex = options.subtitleStreamIndex,
					allowVideoStreamCopy = true,
					allowAudioStreamCopy = true,
					autoOpenLiveStream = true,
				)
			).content
		}

		if (response.errorCode != null) {
			Timber.e("getVideoStreamInfoInternal: Server returned error code: ${response.errorCode}")
			return Result.failure(PlaybackException().apply {
				errorCode = response.errorCode!!
			})
		}

		val streamInfo = createStreamInfo(api, options, response)
		val mediaStreams = streamInfo.mediaSource?.mediaStreams ?: emptyList()
		val audioStreams = mediaStreams.filter { it.type?.name == "Audio" }
		val selectedAudioStream = audioStreams.find { it.index == options.audioStreamIndex }

		Timber.d("getVideoStreamInfoInternal: Created stream info. PlayMethod: ${streamInfo.playMethod}")
		Timber.d("MediaSource: ${streamInfo.mediaSource?.path}")
		if (audioStreams.isNotEmpty()) {
			Timber.d("Available audio streams: ${audioStreams.joinToString(", ") { "[Index: ${it.index}, Codec: ${it.codec ?: "N/A"}, Language: ${it.language ?: "Unknown"}, Default: ${it.isDefault}, Forced: ${it.isForced}]" }}")
		} else {
			Timber.w("No audio streams available in media source")
		}
		Timber.d("Selected audio stream: [Index: ${selectedAudioStream?.index ?: -1}, Codec: ${selectedAudioStream?.codec ?: "N/A"}, Language: ${selectedAudioStream?.language ?: "Unknown"}]")
		Timber.d("Audio stream index in options: ${options.audioStreamIndex}")
		return Result.success(streamInfo)
	} catch (e: Exception) {
		Timber.e(e, "getVideoStreamInfoInternal: Playback failed: ${e.message}")

		// If AC3/E-AC3 was enabled and playback failed, try again with AC3/E-AC3 disabled
		fun isAc3Error(message: String?): Boolean {
			if (message == null) return false
			val lowerMessage = message.lowercase()
			return lowerMessage.contains("ac3") ||
				lowerMessage.contains("eac3") ||
				lowerMessage.contains("audio codec not supported") ||
				lowerMessage.contains("audio track error") ||
				lowerMessage.contains("audio playback failed") ||
				lowerMessage.contains("unsupported audio codec") ||
				lowerMessage.contains("audio codec not recognized") ||
				lowerMessage.contains("audio codec error") ||
				lowerMessage.contains("audio renderer error") ||
				lowerMessage.contains("audio initialization failed") ||
				lowerMessage.contains("eac3-joc") ||
				lowerMessage.contains("eac3_joc")
		}

		// Check for E-AC3 in the media source or any other indicators
		val hasEac3 = options.mediaSourceId?.lowercase()?.contains("eac3") == true ||
				e.stackTraceToString().lowercase().contains("eac3") ||
				e.stackTraceToString().lowercase().contains("eac3-joc")

		val isAc3RelatedError = isAc3Error(e.message) ||
				e.cause?.let { cause -> isAc3Error(cause.message) } == true ||
				e.stackTraceToString().lowercase().contains("ac3") ||
				hasEac3

		if (attemptAc3 && isAc3RelatedError) {
			// Log the fallback attempt
			Timber.w("=== AC3 FALLBACK TRIGGERED ===")
			Timber.w("Original audio stream index: ${options.audioStreamIndex}")
			Timber.w("Error: ${e.message}")
			Timber.w("Cause: ${e.cause?.message}")

			// new options with audio stream reset
			val fallbackOptions = VideoOptions().apply {
				itemId = options.itemId
				mediaSourceId = options.mediaSourceId
				audioStreamIndex = -1 // Let server auto-select a compatible track
				subtitleStreamIndex = options.subtitleStreamIndex
				enableDirectPlay = options.enableDirectPlay
				enableDirectStream = options.enableDirectStream
				maxAudioChannels = options.maxAudioChannels
				profile = options.profile
			}

			// Retry with the new options
			return getVideoStreamInfoInternal(fallbackOptions, startTimeTicks, attemptAc3 = false)
		}

		return Result.failure(e)
	}
}
}
