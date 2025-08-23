package org.jellyfin.androidtv.ui.presentation
import android.view.View
import androidx.core.view.isVisible
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter
import timber.log.Timber

// Custom presenter for Leanback list rows with configurable padding and header visibility
open class CustomListRowPresenter @JvmOverloads constructor(
	private val topPadding: Int? = null
) : ListRowPresenter() {
	init {
		Timber.d("Initializing CustomListRowPresenter with topPadding=%s", topPadding)
		headerPresenter = CustomRowHeaderPresenter()
	}

	override fun isUsingDefaultShadow() = false.also { Timber.d("Disabling default shadow for list row") }

	override fun onSelectLevelChanged(holder: RowPresenter.ViewHolder?) = Unit

	override fun onBindRowViewHolder(holder: RowPresenter.ViewHolder?, item: Any?) {
		Timber.d("Binding row view holder, item=%s", item?.javaClass?.simpleName)
		super.onBindRowViewHolder(holder, item)

		val view = holder?.view?.parent as? View ?: return.also { Timber.d("Parent view is null, skipping padding and header setup") }
		if (topPadding != null) {
			Timber.d("Applying top padding: %d", topPadding)
			view.setPadding(view.paddingLeft, topPadding, view.paddingRight, view.paddingBottom)
		}

		// Hide header view when the item doesn't have one
		holder.headerViewHolder.view.isVisible = !(item is ListRow && item.headerItem == null).also {
			Timber.d("Setting header visibility: %b", it)
		}
	}
}
