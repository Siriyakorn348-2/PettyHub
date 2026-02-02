package com.neatroots.newdog.utils // ปรับ package ตามโครงสร้างโปรเจกต์ของคุณ

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
        outRect.bottom = spacing // เพิ่มช่องว่างด้านล่าง
        outRect.top = topSpacing // เพิ่มช่องว่างด้านบน (ถ้ามี)

        // ถ้าต้องการเพิ่มช่องว่างด้านซ้ายและขวาด้วย (ถ้าไม่ต้องการให้ลบออก)
        outRect.left = 0
        outRect.right = 0
    }
}