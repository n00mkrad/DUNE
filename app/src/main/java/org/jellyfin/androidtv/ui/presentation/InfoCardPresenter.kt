package org.jellyfin.androidtv.ui.presentation
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import org.jellyfin.androidtv.ui.card.MediaInfoCardView
import org.jellyfin.sdk.model.api.MediaStream
import timber.log.Timber

// Presenter for displaying media stream information cards in Leanback UI
class InfoCardPresenter : Presenter() {
	class ViewHolder(
		private val mediaInfoCardView: MediaInfoCardView
	) : Presenter.ViewHolder(mediaInfoCardView) {
		init {
			Timber.d("Initializing ViewHolder for MediaInfoCardView")
		}

		fun setItem(mediaStream: MediaStream) = mediaInfoCardView.setMediaStream(mediaStream)
	}

	override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
		Timber.d("Creating ViewHolder for InfoCardPresenter")
		val view = MediaInfoCardView(parent.context).apply {
			isFocusable = true
			isFocusableInTouchMode = true
		}

		return ViewHolder(view)
	}

	override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any?) {
		Timber.d("Binding view holder, item type=%s", item?.javaClass?.simpleName)
		if (item !is MediaStream) {
			Timber.d("Item is not a MediaStream, skipping binding")
			return
		}
		if (viewHolder !is ViewHolder) {
			Timber.d("ViewHolder is not of type InfoCardPresenter.ViewHolder, skipping binding")
			return
		}

		viewHolder.setItem(item)
	}

	override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) = Unit.also {
		Timber.d("Unbinding view holder, holder is %s", if (viewHolder != null) "non-null" else "null")
	}
}
