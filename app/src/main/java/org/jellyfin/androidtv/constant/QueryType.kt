package org.jellyfin.androidtv.constant
import timber.log.Timber

// Enum defining query types for Jellyfin API requests
enum class QueryType {
	Items,
	NextUp,
	Views,
	Season,
	Upcoming,
	SimilarSeries,
	SimilarMovies,
	StaticPeople,
	StaticChapters,
	Search,
	Specials,
	AdditionalParts,
	Trailers,
	LiveTvChannel,
	LiveTvProgram,
	LiveTvRecording,
	StaticItems,
	StaticAudioQueueItems,
	Artists,
	AlbumArtists,
	AudioPlaylists,
	LatestItems,
	SeriesTimer,
	Premieres,
	Resume;

	companion object {
		init {
			Timber.d("Initializing QueryType enum with %d values", values().size)
		}
	}
}
