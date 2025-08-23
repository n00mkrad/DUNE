package org.jellyfin.androidtv.ui.presentation
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.card.LegacyImageCardView
import org.jellyfin.androidtv.util.ImageHelper
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.sdk.model.api.ImageType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.itemhandling.BaseRowItem
import org.jellyfin.androidtv.util.ImagePreloader
import timber.log.Timber

// Presenter for displaying user view cards with images and titles in Leanback UI
class UserViewCardPresenter(
	val small: Boolean,
) : Presenter(), KoinComponent {
	private val imageHelper by inject<ImageHelper>()

	init {
		Timber.d("Initializing UserViewCardPresenter with small=%b", small)
	}

	inner class ViewHolder(
		private val cardView: LegacyImageCardView
	) : Presenter.ViewHolder(cardView) {
		init {
			Timber.d("Initializing ViewHolder for LegacyImageCardView")
		}

		fun setItem(rowItem: BaseRowItem?) {
			Timber.d("Setting item: %s", rowItem?.getName(cardView.context) ?: "null")
			val baseItem = rowItem?.baseItem

			// Load image
			val image = baseItem?.itemImages[ImageType.PRIMARY]
			cardView.mainImageView.load(
				url = image?.let(imageHelper::getImageUrl),
				blurHash = image?.blurHash,
				placeholder = ContextCompat.getDrawable(cardView.context, R.drawable.tile_land_folder),
				aspectRatio = ImageHelper.ASPECT_RATIO_16_9,
				blurHashResolution = 32,
			)

			// Set title
			cardView.setTitleText(rowItem?.getName(cardView.context))

			// Set size
			if (small) {
				Timber.d("Setting small card size: 133x75")
				cardView.setMainImageDimensions(133, 75)
			} else {
				Timber.d("Setting large card size: 224x126")
				cardView.setMainImageDimensions(224, 126)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
		Timber.d("Creating ViewHolder for UserViewCardPresenter")
		val cardView = LegacyImageCardView(parent.context, true)
		cardView.isFocusable = true
		cardView.isFocusableInTouchMode = true

		val typedValue = TypedValue()
		val theme = parent.context.theme
		theme.resolveAttribute(R.attr.cardViewBackground, typedValue, true)
		@ColorInt val color = typedValue.data
		cardView.setBackgroundColor(color)

		return ViewHolder(cardView)
	}

	override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any?) {
		Timber.d("Binding view holder, item type=%s", item?.javaClass?.simpleName)
		if (item !is BaseRowItem) {
			Timber.d("Item is not a BaseRowItem, skipping binding")
			return
		}
		if (viewHolder !is ViewHolder) {
			Timber.d("ViewHolder is not of type ViewHolder, skipping binding")
			return
		}

		viewHolder.setItem(item)
	}

	override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {
		Timber.d("Unbinding view holder, holder is %s", if (viewHolder != null) "non-null" else "null")
		(viewHolder as? ViewHolder)?.setItem(null)
	}
}
