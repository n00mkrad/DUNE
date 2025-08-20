package org.jellyfin.androidtv.ui.base.modifier

import androidx.compose.foundation.focusGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import timber.log.Timber

/**
 * A focus group that automatically restores the focus to the child previously focused, instead of doing a focus search.
 * Any other focus related modifiers should be added after this one.
 *
 * This modifier includes detailed logging for troubleshooting focus-related issues.
 * Enable Timber debug logging to see focus events in the logcat.
 *
 * @param focusRequester The [FocusRequester] to use for managing focus restoration.
 * @param debugTag Optional tag to identify this focus restorer in logs.
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.childFocusRestorer(
    focusRequester: FocusRequester = remember { FocusRequester() },
    debugTag: String = "FocusRestorer"
): Modifier {
    // Oh look, i changed something 
    LaunchedEffect(focusRequester) {
        Timber.d("[$debugTag] FocusRequester created/updated: ${focusRequester.hashCode()}")
    }

    return focusRequester(focusRequester)
        .focusProperties {
            exit = {
                Timber.d("[$debugTag] Exit focus - saving focused child")
                try {
                    focusRequester.saveFocusedChild()
                    Timber.d("[$debugTag] Successfully saved focused child")
                } catch (e: Exception) {
                    Timber.e(e, "[$debugTag] Failed to save focused child")
                }
                FocusRequester.Default
            }
            enter = {
                Timber.d("[$debugTag] Enter focus - attempting to restore focus")
                try {
                    val result = focusRequester.restoreFocusedChild()
                    if (result) {
                        Timber.d("[$debugTag] Successfully restored focus to previous child")
                        FocusRequester.Cancel
                    } else {
                        Timber.d("[$debugTag] No previous focus to restore, using default behavior")
                        FocusRequester.Default
                    }
                } catch (e: Exception) {
                    Timber.e(e, "[$debugTag] Error while restoring focus")
                    FocusRequester.Default
                }
            }
        }
        .focusGroup()
        .also {
            Timber.d("[$debugTag] Focus restorer initialized")
        }
}
