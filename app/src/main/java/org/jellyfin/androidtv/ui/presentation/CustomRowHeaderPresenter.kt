package org.jellyfin.androidtv.ui.presentation
import androidx.leanback.widget.RowHeaderPresenter
import timber.log.Timber

// Custom presenter for Leanback row headers with disabled selection animation
class CustomRowHeaderPresenter : RowHeaderPresenter() {
	init {
		Timber.d("Initializing CustomRowHeaderPresenter")
	}

	override fun onSelectLevelChanged(holder: ViewHolder?) = Unit.also {
		Timber.d("onSelectLevelChanged called, holder is %s", if (holder != null) "non-null" else "null")
	}
}
