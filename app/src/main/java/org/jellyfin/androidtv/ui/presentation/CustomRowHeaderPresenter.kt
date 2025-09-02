package org.jellyfin.androidtv.ui.presentation

import androidx.annotation.NonNull
import androidx.leanback.widget.RowHeaderPresenter

class CustomRowHeaderPresenter : RowHeaderPresenter() {
    @Suppress("UNUSED_PARAMETER")
    override fun onSelectLevelChanged(holder: ViewHolder) {
        // No action needed
    }
}
