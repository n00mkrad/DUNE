package org.jellyfin.androidtv.ui.itemhandling
import org.jellyfin.sdk.model.api.BaseItemDto

// Audio queue row item
class AudioQueueBaseRowItem(
	item: BaseItemDto
) : BaseItemDtoBaseRowItem(
	item = item,
	staticHeight = true
) {
	// Playing state
	var playing: Boolean = false
}
