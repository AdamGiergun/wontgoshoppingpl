package eu.adamgiergun.wontgoshoppingpl.day

import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.db.Event
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DayTest {

    abstract inner class SetEvent(protected val event: Event) {
        abstract inner class GetDay {
            private val day = Day(event)
            protected lateinit var expectedTime: String
            protected lateinit var expectedName: String
            protected var expectedCalendarEventId: Long? = null
            protected var expectedIsAddedToCalendar = false
            protected var expectedIsDateInMillis = 0L
            protected var expectedAllDay = false
            protected var expectedBackgroundColorResource = 0
            protected var expectedBackgroundResource = 0
            protected lateinit var earlierDay: Day
            protected lateinit var laterDay: Day
            protected lateinit var expectedDateAndWeekdayString: String
            protected lateinit var expectedDescriptionForDialog: String
            protected lateinit var expectedWidgetText: String
            protected var expectedIsCommerceSunday = false

            open fun getTime() {
                assertThat(day.time, `is`(expectedTime))
            }

            open fun getName() {
                assertThat(day.name, `is`(expectedName))
            }

            open fun getCalendarEventId() {
                assertThat(day.calendarEventId, `is`(expectedCalendarEventId))
            }

            open fun isAddedToCalendar() {
                assertThat(day.isAddedToCalendar, `is`(expectedIsAddedToCalendar))
            }

            open fun getDateInMillis() {
                assertThat(day.dateInMillis, `is`(expectedIsDateInMillis))
            }

            open fun getAllDay() {
                assertThat(day.allDay, `is`(expectedAllDay))
            }

            open fun getBackgroundColorResource() {
                assertThat(day.backgroundColorResource, `is`(expectedBackgroundColorResource))
            }

            open fun getBackgroundResource() {
                assertThat(day.backgroundResource, `is`(expectedBackgroundResource))
            }

            open fun compareToSameDay() {
                assertThat(day.compareTo(Day(event)), `is`(0))
            }

            open fun compareToEarlierDay() {
                assertThat(day.compareTo(earlierDay), `is`(1))
            }

            open fun compareToLaterDay() {
                assertThat(day.compareTo(laterDay), `is`(-1))
            }

            open fun getDateAndWeekdayString() {
                assertThat(day.dateAndWeekdayString, `is`(expectedDateAndWeekdayString))
            }

            open fun getDescriptionForDialog() {
                assertThat(day.descriptionForDialog, `is`(expectedDescriptionForDialog))
            }

            open fun getWidgetText() {
                assertThat(day.widgetText, `is`(expectedWidgetText))
            }

            open fun isCommerceSunday() {
                assertThat(day.isCommerceSunday, `is`(expectedIsCommerceSunday))
            }
        }
    }

    @Nested
    @DisplayName("Given Event of day type NON_COMMERCE_DAY, item 2020_01_05,00:00,Non-commerce Sunday, calendarId and calendarEventId nulls")
    inner class SetEvent20200105 : SetEvent(
        Event(
            0,
            Day.Type.NON_COMMERCE_DAY.code,
            "2020_01_05,00:00,Non-commerce Sunday",
            null,
            null
        )
    ) {
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
            @DisplayName("Then calendarEventId is null")
            override fun getCalendarEventId() {
                expectedCalendarEventId = null
                super.getCalendarEventId()
            }

            @Test
            @DisplayName("Then isAddedToCalendar is false")
            override fun isAddedToCalendar() {
                expectedIsAddedToCalendar = false
                super.isAddedToCalendar()
            }

            @Test
            @DisplayName("Then dateInMillis is 1578182400000L")
            override fun getDateInMillis() {
                expectedIsDateInMillis = 1578182400000L
                super.getDateInMillis()
            }

            @Test
            @DisplayName("Then allDay is true")
            override fun getAllDay() {
                expectedAllDay = true
                super.getAllDay()
            }

            @Test
            @DisplayName("Then backgroundColorResource is colorNonCommerceDay")
            override fun getBackgroundColorResource() {
                expectedBackgroundColorResource = R.color.colorNonCommerceDay
                super.getBackgroundColorResource()
            }

            @Test
            @DisplayName("Then backgroundResource is card_bg_orange drawable")
            override fun getBackgroundResource() {
                expectedBackgroundResource = R.drawable.card_bg_orange
                super.getBackgroundResource()
            }

            @Test
            @DisplayName("Then another day instance from the same event is equal")
            override fun compareToSameDay() {
                super.compareToSameDay()
            }

            @Test
            @DisplayName("Then day with earlier date is lesser")
            override fun compareToEarlierDay() {
                earlierDay = Day(
                    Event(
                        0,
                        Day.Type.NON_COMMERCE_DAY.code,
                        "2019_12_31,00:00,Non-commerce Sunday",
                        null,
                        null
                    )
                )
                super.compareToEarlierDay()
            }

            @Test
            @DisplayName("Then day with later date is bigger")
            override fun compareToLaterDay() {
                laterDay = Day(
                    Event(
                        0,
                        Day.Type.NON_COMMERCE_DAY.code,
                        "2020_01_06,00:00,Non-commerce Sunday",
                        null,
                        null
                    )
                )
                super.compareToLaterDay()
            }

            @Test
            @DisplayName("Then dateAndWeekdayString is 5 sty 2020\nniedziela")
            override fun getDateAndWeekdayString() {
                expectedDateAndWeekdayString = "5 sty 2020\nniedziela"
                super.getDateAndWeekdayString()
            }

            @Test
            @DisplayName("Then descriptionForDialog is 5 sty 2020\nniedziela")
            override fun getDescriptionForDialog() {
                expectedDescriptionForDialog = "5 sty 2020\nniedziela"
                super.getDescriptionForDialog()
            }

            @Test
            @DisplayName("Then widgetText is 5 sty 2020\nPL: Non-commerce Sunday")
            override fun getWidgetText() {
                expectedWidgetText = "5 sty 2020\nPL: Non-commerce Sunday"
                super.getWidgetText()
            }

            @Test
            @DisplayName("Then isCommerceSunday is false")
            override fun isCommerceSunday() {
                expectedIsCommerceSunday = false
                super.isCommerceSunday()
            }
        }
    }

    @Nested
    @DisplayName("Given Event of day type NON_COMMERCE_DAY, 2020_04_11,13:00,Holy Saturday, calendarId is calendar and calendarEventId is 123")
    inner class SetEvent20200411 : SetEvent(
        Event(
            0,
            Day.Type.NON_COMMERCE_DAY.code,
            "2020_04_11,13:00,Holy Saturday,",
            "calendar",
            123
        )
    ) {
        @Nested
        @DisplayName("When Day(event)")
        inner class TestDay : GetDay() {
            @Test
            @DisplayName("Then time is 13:00")
            override fun getTime() {
                expectedTime = "13:00"
                super.getTime()
            }

            @Test
            @DisplayName("Then name is Holy Saturday")
            override fun getName() {
                expectedName = "Holy Saturday"
                super.getName()
            }

            @Test
            @DisplayName("Then calendarEventId is 123")
            override fun getCalendarEventId() {
                expectedCalendarEventId = 123L
                super.getCalendarEventId()
            }

            @Test
            @DisplayName("Then isAddedToCalendar is true")
            override fun isAddedToCalendar() {
                expectedIsAddedToCalendar = true
                super.isAddedToCalendar()
            }

            @Test
            @DisplayName("Then dateInMillis is 1586563200000L")
            override fun getDateInMillis() {
                expectedIsDateInMillis = 1586563200000L
                super.getDateInMillis()
            }

            @Test
            @DisplayName("Then allDay is false")
            override fun getAllDay() {
                expectedAllDay = false
                super.getAllDay()
            }

            @Test
            @DisplayName("Then backgroundColorResource is colorNonCommerceDay")
            override fun getBackgroundColorResource() {
                expectedBackgroundColorResource = R.color.colorNonCommerceDay
                super.getBackgroundColorResource()
            }

            @Test
            @DisplayName("Then backgroundResource is card_bg_orange drawable")
            override fun getBackgroundResource() {
                expectedBackgroundResource = R.drawable.card_bg_orange
                super.getBackgroundResource()
            }

            @Test
            @DisplayName("Then another day instance from the same event is equal")
            override fun compareToSameDay() {
                super.compareToSameDay()
            }

            @Test
            @DisplayName("Then dateAndWeekdayString is 11 kwi 2020\nsobota")
            override fun getDateAndWeekdayString() {
                expectedDateAndWeekdayString = "11 kwi 2020\nsobota"
                super.getDateAndWeekdayString()
            }

            @Test
            @DisplayName("Then descriptionForDialog is 11 kwi 2020\nsobota")
            override fun getDescriptionForDialog() {
                expectedDescriptionForDialog = "11 kwi 2020\nsobota"
                super.getDescriptionForDialog()
            }

            @Test
            @DisplayName("Then widgetText is 11 kwi 2020\nPL: Holy Saturday")
            override fun getWidgetText() {
                expectedWidgetText = "11 kwi 2020\nPL: Holy Saturday"
                super.getWidgetText()
            }

            @Test
            @DisplayName("Then isCommerceSunday is false")
            override fun isCommerceSunday() {
                expectedIsCommerceSunday = false
                super.isCommerceSunday()
            }
        }
    }
}
