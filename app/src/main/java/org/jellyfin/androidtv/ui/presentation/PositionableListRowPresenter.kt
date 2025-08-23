package org.jellyfin.androidtv.ui.presentation
import android.content.Context
import android.view.View
import androidx.leanback.widget.RowHeaderPresenter
import androidx.leanback.widget.RowPresenter
import org.jellyfin.androidtv.R
import timber.log.Timber

// Presenter for positionable Leanback list rows with custom header visibility and selection
class PositionableListRowPresenter : CustomListRowPresenter {
	private var viewHolder: ViewHolder? = null

	// Backward compatible constructor
	@JvmOverloads
	constructor(padding: Int = 0) : super(padding) {
		Timber.d("Initializing PositionableListRowPresenter with padding=%d", padding)
		init()
	}

	// New constructor with context and spacing preference
	constructor(context: Context, useLargeSpacing: Boolean = false) : this(
		context.resources.getDimensionPixelSize(
			if (useLargeSpacing) R.dimen.home_row_spacing_large
			else R.dimen.home_row_spacing
		)
	) {
		Timber.d("Initializing PositionableListRowPresenter with context, useLargeSpacing=%b", useLargeSpacing)
	}

	private fun init() {
		Timber.d("Configuring PositionableListRowPresenter settings")
		shadowEnabled = false
		selectEffectEnabled = true

		// Configure header to always be visible
		Timber.d("Setting custom header presenter")
		headerPresenter = object : RowHeaderPresenter() {
			override fun onSelectLevelChanged(holder: ViewHolder) {
				Timber.d("Header onSelectLevelChanged called, holder is %s", if (holder != null) "non-null" else "null")
				super.onSelectLevelChanged(holder)
				// Keep header always visible
				holder.view.alpha = 1f
			}
		}
	}

	override fun isUsingDefaultListSelectEffect() = true.also { Timber.d("Enabling default list select effect") }

	override fun isUsingDefaultShadow() = false.also { Timber.d("Disabling default shadow") }

	override fun onRowViewExpanded(viewHolder: RowPresenter.ViewHolder, expanded: Boolean) {
		Timber.d("Row view expanded, expanded=%b", expanded)
		super.onRowViewExpanded(viewHolder, expanded)
		// Ensure header is always visible when row is expanded
		viewHolder.headerViewHolder?.view?.visibility = View.VISIBLE
		viewHolder.headerViewHolder?.view?.alpha = 1f
	}

	override fun onSelectLevelChanged(holder: RowPresenter.ViewHolder?) {
		Timber.d("Row onSelectLevelChanged called, holder is %s", if (holder != null) "non-null" else "null")
		super.onSelectLevelChanged(holder)
		// Keep header visible when row is selected
		holder?.headerViewHolder?.view?.visibility = View.VISIBLE
		holder?.headerViewHolder?.view?.alpha = 1f
	}

	override fun onBindRowViewHolder(holder: RowPresenter.ViewHolder?, item: Any?) {
		Timber.d("Binding row view holder, item=%s", item?.javaClass?.simpleName)
		super.onBindRowViewHolder(holder, item)
		if (holder !is ViewHolder) {
			Timber.d("ViewHolder is not of type ViewHolder, skipping binding")
			return
		}

		viewHolder = holder
	}

	var position: Int
		get() = viewHolder?.gridView?.selectedPosition ?: -1.also { Timber.d("Getting position: %d", it) }
		set(value) {
			Timber.d("Setting position to %d", value)
			viewHolder?.gridView?.selectedPosition = value
		}
}
