import android.app.Activity
import android.util.Log
import com.neatroots.newdog.Model.EventModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class EventDecorator(
    private val activity: Activity,
    private val eventMap: Map<CalendarDay, List<EventModel>>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return eventMap.containsKey(day) && eventMap[day]?.isNotEmpty() == true
    }

    override fun decorate(view: DayViewFacade) {
    }

    inner class EventDayDecorator(private val day: CalendarDay) : DayViewDecorator {
        override fun shouldDecorate(calendarDay: CalendarDay): Boolean {
            return calendarDay == day
        }

        override fun decorate(view: DayViewFacade) {
            val events = eventMap[day] ?: return
            Log.d("EventDebug", "ตกแต่ง $day: มี ${events.size} อีเวนต์")
            val colors = events.map { event ->
                Log.d("EventDebug", "อีเวนต์: ${event.title}, Color: ${event.color}")
                event.color
            }
            view.addSpan(MultiDotSpan(8f, colors))
        }
    }

    fun getDecorators(): List<DayViewDecorator> {
        return eventMap.keys.map { EventDayDecorator(it) }
    }
}