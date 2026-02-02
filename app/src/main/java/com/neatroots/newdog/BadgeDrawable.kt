package com.neatroots.newdog

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class BadgeDrawable(context: Context) {
    var number: Int = 0
    var backgroundDrawable: Drawable? = null

    init {
        backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.notification_badge)
    }
}