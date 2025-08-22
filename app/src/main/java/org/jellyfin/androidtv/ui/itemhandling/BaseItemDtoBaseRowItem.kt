package org.jellyfin.androidtv.ui.itemhandling
import android.content.Context
import org.jellyfin.androidtv.constant.ImageType
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.androidtv.util.TimeUtils
import org.jellyfin.androidtv.util.apiclient.seriesPrimaryImage
import org.jellyfin.androidtv.util.getTimeFormatter
import org.jellyfin.androidtv.util.locale
import org.jellyfin.androidtv.util.sdk.getFullName
import org.jellyfin.androidtv.util.sdk.getSubName
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.extensions.ticks
import java.time.format.DateTimeFormatter

// Base item row class
open class BaseItemDtoBaseRowItem @JvmOverloads constructor(
	item: BaseItemDto,
	preferParentThumb: Boolean = false,
	staticHeight: Boolean = false,
	selectAction: BaseRowItemSelectAction = BaseRowItemSelectAction.ShowDetails,
	val preferSeriesPoster: Boolean = false
) : BaseRowItem(
	baseRowType = when (item.type) {
		BaseItemKind.TV_CHANNEL,BaseItemKind.LIVE_TV_CHANNEL -> BaseRowType.LiveTvChannel
		BaseItemKind.PROGRAM,BaseItemKind.TV_PROGRAM,BaseItemKind.LIVE_TV_PROGRAM -> BaseRowType.LiveTvProgram
		BaseItemKind.RECORDING -> BaseRowType.LiveTvRecording
		else -> BaseRowType.BaseItem
	},
	staticHeight = staticHeight,
	preferParentThumb = preferParentThumb,
	selectAction = selectAction,
	baseItem = item
) {
	// Card overlay visibility
	override val showCardInfoOverlay get() = when(baseItem?.type) {
		BaseItemKind.FOLDER,BaseItemKind.PHOTO_ALBUM,BaseItemKind.USER_VIEW,BaseItemKind.COLLECTION_FOLDER,
		BaseItemKind.PHOTO,BaseItemKind.VIDEO,BaseItemKind.PERSON,BaseItemKind.PLAYLIST,BaseItemKind.MUSIC_ARTIST -> true
		else -> false
	}

	// Item ID
	override val itemId get() = baseItem?.id

	// Favorite and played status
	override val isFavorite get() = baseItem?.userData?.isFavorite == true
	override val isPlayed get() = baseItem?.userData?.played == true

	// Child count string
	val childCountStr: String? get() {
		if(baseItem?.type == BaseItemKind.PLAYLIST) {
			val childCount = baseItem.cumulativeRunTimeTicks?.ticks?.let { duration ->
				TimeUtils.formatMillis(duration.inWholeMilliseconds)
			}
			if(childCount != null) return childCount
		}
		if(baseItem?.isFolder == true && baseItem.type != BaseItemKind.MUSIC_ARTIST) {
			val childCount = baseItem.childCount
			if(childCount != null && childCount > 0) return childCount.toString()
		}
		return null
	}

	// Card name
	override fun getCardName(context: Context) = when {
		baseItem?.type == BaseItemKind.AUDIO && baseItem.artists != null -> baseItem.artists?.joinToString(", ")
		baseItem?.type == BaseItemKind.AUDIO && baseItem.albumArtists != null -> baseItem.albumArtists?.joinToString(", ")
		baseItem?.type == BaseItemKind.AUDIO && baseItem.albumArtist != null -> baseItem.albumArtist
		baseItem?.type == BaseItemKind.AUDIO && baseItem.album != null -> baseItem.album
		else -> baseItem?.getFullName(context)
	}

	// Full name
	override fun getFullName(context: Context) = baseItem?.getFullName(context)

	// Name
	override fun getName(context: Context) = when(baseItem?.type) {
		BaseItemKind.AUDIO -> baseItem.getFullName(context)
		else -> baseItem?.name
	}

	// Summary
	override fun getSummary(context: Context) = baseItem?.overview

	// Subtext
	override fun getSubText(context: Context) = when(baseItem?.type) {
		BaseItemKind.TV_CHANNEL -> baseItem.number
		BaseItemKind.TV_PROGRAM,BaseItemKind.PROGRAM -> baseItem.episodeTitle ?: baseItem.channelName
		BaseItemKind.RECORDING -> {
			val title = listOfNotNull(baseItem.channelName,baseItem.episodeTitle).joinToString(" - ")
			val timestamp = buildString {
				append(DateTimeFormatter.ofPattern("d MMM",context.locale).format(baseItem.startDate))
				append(" ")
				append(context.getTimeFormatter().format(baseItem.startDate))
				append(" - ")
				append(context.getTimeFormatter().format(baseItem.endDate))
			}
			"$title $timestamp"
		}
		else -> baseItem?.getSubName(context)
	}

	// Image URL
	override fun getImageUrl(context: Context,imageHelper: ImageHelper,imageType: ImageType,fillWidth: Int,fillHeight: Int): String? {
		val seriesPrimaryImage = baseItem?.seriesPrimaryImage
		return when {
			preferSeriesPoster && seriesPrimaryImage != null -> imageHelper.getImageUrl(seriesPrimaryImage)
			imageType == ImageType.BANNER -> imageHelper.getBannerImageUrl(requireNotNull(baseItem),fillWidth,fillHeight)
			imageType == ImageType.THUMB -> imageHelper.getThumbImageUrl(requireNotNull(baseItem),fillWidth,fillHeight)
			else -> {
				val item = baseItem ?: return null
				val useParentThumb = when(item.type) {
					BaseItemKind.EPISODE,BaseItemKind.SEASON -> preferParentThumb
					else -> false
				}
				imageHelper.getPrimaryImageUrl(item,useParentThumb,null,fillHeight)
			}
		}
	}

	// Badge image
	override fun getBadgeImage(context: Context,imageHelper: ImageHelper) = null
}
