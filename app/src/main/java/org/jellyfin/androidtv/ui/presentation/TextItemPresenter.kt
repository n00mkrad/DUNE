package org.jellyfin.androidtv.ui.presentation
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.Presenter
import timber.log.Timber

// Presenter for displaying text items in a Leanback TextView
class TextItemPresenter : Presenter() {
	companion object {
		private const val ITEM_WIDTH = 400
		private const val ITEM_HEIGHT = 200
		private const val TEXT_SIZE = 32f
	}

	override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
		Timber.d("Creating ViewHolder for TextItemPresenter")
		val view = TextView(parent.context).apply {
			layoutParams = ViewGroup.LayoutParams(ITEM_WIDTH, ITEM_HEIGHT)
			isFocusable = true
			isFocusableInTouchMode = true
			setTextColor(Color.WHITE)
			gravity = Gravity.CENTER
			textSize = TEXT_SIZE
		}

		return ViewHolder(view)
	}

	override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
		Timber.d("Binding view holder, item=%s", item?.toString() ?: "null")
		(viewHolder?.view as? TextView)?.text = item.toString()
	}

	override fun onUnbindViewHolder(viewHolder: ViewHolder?) = Unit.also {
		Timber.d("Unbinding view holder, holder is %s", if (viewHolder != null) "non-null" else "null")
	}
}
