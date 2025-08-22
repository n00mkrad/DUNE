package org.jellyfin.androidtv.constant

enum class QueryType {
	// General content queries
	Items,
	Search,
	StaticItems,
	LatestItems,
	Resume,

	// Series-related queries
	SeriesTimer,
	Season,
	SimilarSeries,
	Specials,

	// Movie-related queries
	SimilarMovies,
	Premieres,
	Upcoming,

	// Library and view queries
	Views,
	StaticChapters,

	// Media-specific queries
	AdditionalParts,
	Trailers,

	// Live TV queries
	LiveTvChannel,
	LiveTvProgram,
	LiveTvRecording,

	// Audio-related queries
	Artists,
	AlbumArtists,
	AudioPlaylists,
	StaticAudioQueueItems,

	// People queries
	StaticPeople,

	// Playback queries
	NextUp
}
