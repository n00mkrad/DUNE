package org.jellyfin.playback.jellyfin

import org.jellyfin.playback.core.plugin.playbackPlugin
import org.jellyfin.playback.jellyfin.mediastream.JellyfinMediaStreamResolver
import org.jellyfin.playback.jellyfin.playsession.PlaySessionService
import org.jellyfin.playback.jellyfin.playsession.PlaySessionSocketService
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.DeviceProfile

/**
 * Creates a playback plugin for Jellyfin integration.
 *
 * @param api The ApiClient instance for interacting with the Jellyfin server.
 * @param deviceProfileBuilder A lambda to build the DeviceProfile for media streaming.
 * @return A configured playback plugin with Jellyfin-specific services.
 */
fun jellyfinPlugin(
	api: ApiClient,
	deviceProfileBuilder: () -> DeviceProfile,
) = playbackPlugin {
	// Media stream resolver
	provide(JellyfinMediaStreamResolver(api, deviceProfileBuilder))

	// Play session services
	val playSessionService = PlaySessionService(api)
	provide(playSessionService)
	provide(PlaySessionSocketService(api, playSessionService))

	// Lyrics service
	provide(LyricsPlayerService(api))
}
