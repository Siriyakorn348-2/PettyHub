import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.LineBackgroundSpan

class MultiDotSpan(
    private val radius: Float,
    private val colors: List<Int>
) : LineBackgroundSpan {

    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int
    ) {
        val availableWidth = right - left
        val dotSpacing = radius * 1.5f // ลดระยะห่างระหว่างจุดให้ใกล้กันมากขึ้น
        val totalWidth = if (colors.isNotEmpty()) (colors.size - 1) * dotSpacing else 0f
        var currentX = left + (availableWidth - totalWidth) / 2f

        // วางจุดล่างเลขวัน (ใช้ bottom แทนการคำนวณกึ่งกลาง)
        val y = baseline + radius * 1.5f

        colors.forEach { color ->
            paint.color = color
            canvas.drawCircle(currentX, y, radius, paint)
            currentX += dotSpacing
        }
    }
}