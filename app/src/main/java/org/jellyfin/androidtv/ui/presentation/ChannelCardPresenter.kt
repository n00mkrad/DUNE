package org.jellyfin.androidtv.ui.presentation

import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.leanback.widget.Presenter
import org.jellyfin.androidtv.ui.card.ChannelCardView
import org.jellyfin.sdk.model.api.BaseItemDto

class ChannelCardPresenter : Presenter() {
	class ViewHolder(
		private val cardView: ChannelCardView,
	) : Presenter.ViewHolder(cardView) {
		fun setItem(item: BaseItemDto?) = cardView.setItem(item)
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup): ViewHolder {
		val view = ChannelCardView(parent.context).apply {
			isFocusable = true
			isFocusableInTouchMode = true
		}

		return ViewHolder(view)
	}

	// This version is called first by the framework
	override fun onBindViewHolder(
		@NonNull viewHolder: Presenter.ViewHolder,
		@NonNull item: Any,
		@NonNull payloads: MutableList<Any>
	) {
		onBindViewHolder(viewHolder, item)
	}

	// This version is called for backward compatibility
	override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
		if (item !is BaseItemDto) return
		if (viewHolder !is ViewHolder) return

		viewHolder.setItem(item)
	}

	// This version is called by the framework
	@Suppress("UNUSED_PARAMETER")
	override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
		// No cleanup needed
	}
}
