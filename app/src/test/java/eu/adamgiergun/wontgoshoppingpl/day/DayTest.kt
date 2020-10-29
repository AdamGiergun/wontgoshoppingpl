package eu.adamgiergun.wontgoshoppingpl.day

import eu.adamgiergun.wontgoshoppingpl.db.Event
import org.hamcrest.Matchers.`is`
import org.junit.Assert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DayTest {

    abstract inner class SetEvent(private val event: Event) {
        abstract inner class GetDay() {
            private val day = Day(event)
            protected lateinit var expectedTime: String
            protected lateinit var expectedName: String
            protected var expectedCalendarEventId = 0L

            open fun getTime() {
                Assert.assertThat(day.time, `is`(expectedTime))
            }

            open fun getName() {
                Assert.assertThat(day.name, `is`(expectedName))
            }

            open fun getCalendarEventId() {
                Assert.assertThat(day.calendarEventId, `is`(expectedCalendarEventId))
            }
        }
    }
//
//
//        @Test
//        fun isAddedToCalendar() {
//        }
//
//        @Test
//        fun getDateInMillis() {
//        }
//
//        @Test
//        fun getAllDay() {
//        }
//
//        @Test
//        fun getBackgroundColorResource() {
//        }
//
//        @Test
//        fun getBackgroundResource() {
//        }
//
//        @Test
//        fun compareTo() {
//        }
//
//        @Test
//        fun getDateAndWeekdayString() {
//        }
//
//        @Test
//        fun getDescriptionForDialog() {
//        }
//
//        @Test
//        fun getWidgetText() {
//        }
//
//        @Test
//        fun isCommerceSunday() {
//        }
//
//        @Test
//        fun setReminder() {
//        }

    @Nested
    @DisplayName("Given Event of day type NON_COMMERCE_DAY, item 2020_01_05,00:00,Non-commerce Sunday, calendarId and calendarEventId nulls")
    inner class SetEvent1 : SetEvent(Event(0, Day.Type.NON_COMMERCE_DAY.code, "2020_01_05,00:00,Non-commerce Sunday", null, null)) {
        @Nested
        @DisplayName("When Day(event)")
        inner class TestDay : GetDay() {
            @Test
            @DisplayName("Then time is 00:00")
            override fun getTime() {
                expectedTime = "00:00"
                super.getTime()
            }

            @Test
            @DisplayName("Then name is Non-commerce Sunday")
            override fun getName() {
                expectedName = "Non-commerce Sunday"
                super.getName()
            }

            @Test
            @DisplayName("Then calendarEventId is zero")
            override fun getCalendarEventId() {
                expectedCalendarEventId = 0L
                super.getCalendarEventId()
            }
        }
    }
}
