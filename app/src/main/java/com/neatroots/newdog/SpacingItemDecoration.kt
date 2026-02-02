package com.neatroots.newdog.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(
    private val spacing: Int,
    private val topSpacing: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = spacing
        outRect.top = topSpacing


        outRect.left = 0
        outRect.right = 0
    }
}