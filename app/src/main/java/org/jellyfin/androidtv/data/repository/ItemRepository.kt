package org.jellyfin.androidtv.data.repository

import org.jellyfin.sdk.model.api.ItemFields

object ItemRepository {
	val itemFields = setOf(
		// Item metadata
		ItemFields.OVERVIEW,
		ItemFields.GENRES,
		ItemFields.TAGLINES,
		ItemFields.DATE_CREATED,
		ItemFields.PATH,

		// Media information
		ItemFields.MEDIA_SOURCES,
		ItemFields.MEDIA_SOURCE_COUNT,
		ItemFields.MEDIA_STREAMS,
		ItemFields.CHAPTERS,
		ItemFields.TRICKPLAY,

		// Image and display
		ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
		ItemFields.SERIES_PRIMARY_IMAGE,
		ItemFields.DISPLAY_PREFERENCES_ID,

		// Item counts and properties
		ItemFields.CHILD_COUNT,
		ItemFields.ITEM_COUNTS,
		ItemFields.CUMULATIVE_RUN_TIME_TICKS,

		// Permissions
		ItemFields.CAN_DELETE,

		// Channel information
		ItemFields.CHANNEL_INFO
	)
}
