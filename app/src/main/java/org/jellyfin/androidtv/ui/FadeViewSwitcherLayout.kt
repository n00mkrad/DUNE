package org.jellyfin.androidtv.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.IntRange
import android.view.animation.AlphaAnimation

class FadeViewSwitcherLayout @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
	defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

	companion object {
		const val VIEW_NONE = -1
	}

	// Current view state
	private var currentView = VIEW_NONE

	// View index calculations
	private val nextViewId: Int
		get() = if (currentView >= childCount - 1) 0 else currentView + 1
	private val previousViewId: Int
		get() = if (currentView <= 0) childCount - 1 else currentView - 1

	// View management
	override fun onViewAdded(child: View?) {
		super.onViewAdded(child)
		// Hide new views by default
		child?.alpha = 0f
	}

	// View switching methods
	fun showNextView() = showView(nextViewId)
	fun showPreviousView() = showView(previousViewId)
	fun hideAllViews() = showView(VIEW_NONE)

	// View accessors
	fun <V : View> getNextView(): V = getChildAt(nextViewId) as V
	fun <V : View> getCurrentView(): V? = if (currentView == VIEW_NONE) null else getChildAt(currentView) as V
	fun <V : View> getPreviousView(): V = getChildAt(previousViewId) as V

	// View display with fade animation
	fun showView(@IntRange(from = -1) view: Int) {
		// TODO: Implement smarter crossfade to hide old view after new view is fully visible
		if (currentView != VIEW_NONE) {
			getChildAt(currentView).fadeOut()
		}
		if (view != VIEW_NONE) {
			getChildAt(view).fadeIn()
		}
		currentView = view
	}

	// Animation helpers
	private fun View.fadeIn() {
		alpha = 1f
		startAnimation(AlphaAnimation(0f, 1f).apply {
			duration = 20
			fillAfter = true
		})
	}

	private fun View.fadeOut() {
		startAnimation(AlphaAnimation(1f, 0f).apply {
			duration = 20
			fillAfter = true
		})
	}
}
