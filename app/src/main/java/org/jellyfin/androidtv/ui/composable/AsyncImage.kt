package org.jellyfin.androidtv.ui.composable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import org.jellyfin.androidtv.ui.AsyncImageView
import org.jellyfin.androidtv.util.BlurHashDecoder
import androidx.core.graphics.createBitmap

// Composable functions for asynchronous image loading with BlurHash support
private data class AsyncImageState(
	val url: String?,
	val blurHash: String?,
)

@Composable
fun AsyncImage(
	modifier: Modifier = Modifier,
	url: String? = null,
	blurHash: String? = null,
	placeholder: Drawable? = null,
	aspectRatio: Float = 1f,
	blurHashResolution: Int = 22,
	scaleType: ImageView.ScaleType? = null,
) {
	var state by remember(url, blurHash) { mutableStateOf(AsyncImageState(url, blurHash)) }

	AndroidView(
		modifier = modifier,
		factory = { context ->
			AsyncImageView(context).also { view ->
				view.adjustViewBounds = true
				view.scaleType = scaleType ?: ImageView.ScaleType.FIT_CENTER
			}
		},
		update = { view ->
			view.load(
				url = state.url,
				blurHash = state.blurHash,
				placeholder = placeholder,
				aspectRatio = aspectRatio.toDouble(),
				blurHashResolution = blurHashResolution,
			)
		},
	)
}

@Composable
fun blurHashPainter(
	blurHash: String,
	size: IntSize,
	punch: Float = 1f,
): Painter = remember(blurHash, size, punch) {
	val bitmap = BlurHashDecoder.decode(
		blurHash = blurHash,
		width = size.width,
		height = size.height,
		punch = punch,
	)
	bitmap?.let { BitmapPainter(it.asImageBitmap()) } ?: BitmapPainter(createBitmap(1, 1).asImageBitmap())
}
