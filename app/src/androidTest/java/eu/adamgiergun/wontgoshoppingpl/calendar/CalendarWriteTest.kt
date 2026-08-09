package eu.adamgiergun.wontgoshoppingpl.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import android.provider.CalendarContract
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.TimeZone

/**
 * Zapis wydarzenia do prawdziwego dostawcy kalendarza i odczyt z powrotem.
 *
 * Robolectric ma tylko atrapę `ContentResolvera`, więc cała warstwa
 * wejścia-wyjścia [SimpleCalendarEvent] — `write`, `writeReminders`, `exists`,
 * `delete` — była dotąd nieprzetestowana. A to w niej siedziały oba błędy
 * znalezione przy okazji pytania o użytkownika w Australii: sprzeczna strefa
 * przy wydarzeniu całodobowym i przypomnienie liczone względem UTC.
 *
 * Ten test jest zapisem tego, co wcześniej robiłem ręcznie przez
 * `adb shell content query`.
 *
 * Kalendarz zakładany jest lokalny (`ACCOUNT_TYPE_LOCAL`), więc nie dotyka
 * żadnego konta Google ani niczego nie synchronizuje. Sprzątany po każdym teście.
 */
@RunWith(AndroidJUnit4::class)
class CalendarWriteTest {

    private companion object {
        const val KONTO = "wgs-test"
        const val STREFA_WYDARZENIA = "Europe/Warsaw"
        val DZIEN = SimpleDate(2026, 8, 23)
    }

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private var idKalendarza: Long = 0

    // Nie lateinit: gdyby @Before padło przed przypisaniem, @After rzuciłoby
    // UninitializedPropertyAccessException i przesłoniło prawdziwą przyczynę.
    private val domyslnaStrefa: TimeZone = TimeZone.getDefault()

    @Before
    fun przygotujKalendarz() {
        // `pm grant` przez powłokę instrumentacji zamiast UiAutomation
        // .grantRuntimePermission — ta druga bywa niedostępna zależnie od
        // wersji Androida, a powłoka działa wszędzie i nie wymaga zależności.
        nadajUprawnienie("android.permission.READ_CALENDAR")
        nadajUprawnienie("android.permission.WRITE_CALENDAR")
        idKalendarza = zalozKalendarz()
    }

    /**
     * Strumień trzeba wyczytać do końca, inaczej `executeShellCommand` wraca
     * natychmiast, a `pm grant` jeszcze się nie wykonał — pierwsze sięgnięcie do
     * dostawcy kalendarza kończy się wtedy `SecurityException`.
     */
    private fun nadajUprawnienie(uprawnienie: String) {
        val strumien = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant ${context.packageName} $uprawnienie")
        ParcelFileDescriptor.AutoCloseInputStream(strumien).use { it.readBytes() }
        assertThat(
            "nie udało się nadać uprawnienia $uprawnienie",
            context.checkSelfPermission(uprawnienie),
            `is`(PackageManager.PERMISSION_GRANTED)
        )
    }

    @After
    fun posprzataj() {
        TimeZone.setDefault(domyslnaStrefa)
        if (idKalendarza != 0L) {
            context.contentResolver.delete(
                uriSyncAdaptera(CalendarContract.Calendars.CONTENT_URI),
                "${CalendarContract.Calendars._ID} = ?",
                arrayOf(idKalendarza.toString())
            )
        }
    }

    private fun uriSyncAdaptera(uri: android.net.Uri) = uri.buildUpon()
        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, KONTO)
        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
        .build()

    private fun zalozKalendarz(): Long {
        val wartosci = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, KONTO)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.NAME, KONTO)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, KONTO)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0x2196F3)
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, KONTO)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, STREFA_WYDARZENIA)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.VISIBLE, 1)
        }
        val uri = context.contentResolver
            .insert(uriSyncAdaptera(CalendarContract.Calendars.CONTENT_URI), wartosci)
        return ContentUris.parseId(requireNotNull(uri) { "nie udało się założyć kalendarza testowego" })
    }

    private fun zapiszDzien(
        calyDzien: Boolean = true,
        godzina: String = "00:00",
        koniec: Pair<SimpleDate, String>? = DZIEN to "23:59",
        przypomnienie: SimpleReminder? = null,
    ): Long {
        val budowniczy = SimpleCalendarEvent.Builder(
            idKalendarza.toString(), DZIEN, SimpleTime(godzina), STREFA_WYDARZENIA, calyDzien
        ).setTitle("Niedziela niehandlowa")
        koniec?.let { budowniczy.setEndCalendar(it.first, SimpleTime(it.second)) }
        przypomnienie?.let {
            budowniczy.setReminders(SimpleReminder.List().apply { add(it) })
        }
        return budowniczy.build().write(context)
    }

    private fun <T> odczytaj(idWydarzenia: Long, kolumna: String, odczyt: (android.database.Cursor) -> T): T =
        context.contentResolver.query(
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, idWydarzenia),
            arrayOf(kolumna), null, null, null
        )!!.use { kursor ->
            assertThat("brak wydarzenia $idWydarzenia", kursor.moveToFirst(), `is`(true))
            odczyt(kursor)
        }

    // ---------- wydarzenie całodobowe ----------

    @Test
    fun dzien_calodobowy_ladu_je_w_kalendarzu_jako_data_w_UTC() {
        val id = zapiszDzien()

        assertThat(id > 0, `is`(true))
        assertThat(odczytaj(id, CalendarContract.Events.ALL_DAY) { it.getInt(0) }, `is`(1))
        // Dostawca wymaga przy ALL_DAY strefy UTC. Konto lokalne nic nie
        // synchronizuje, więc nie ma tu aliasu „Zulu", którym Google przepisuje
        // tę wartość po stronie serwera.
        assertThat(odczytaj(id, CalendarContract.Events.EVENT_TIMEZONE) { it.getString(0) }, `is`("UTC"))
    }

    @Test
    fun dzien_calodobowy_trwa_dokladnie_dobe() {
        val id = zapiszDzien()

        val start = odczytaj(id, CalendarContract.Events.DTSTART) { it.getLong(0) }
        val koniec = odczytaj(id, CalendarContract.Events.DTEND) { it.getLong(0) }

        assertThat(koniec - start, `is`(24L * 60 * 60 * 1000))
    }

    @Test
    fun dzien_calodobowy_zaczyna_sie_o_polnocy_UTC_swojego_dnia() {
        val id = zapiszDzien()

        val start = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = odczytaj(id, CalendarContract.Events.DTSTART) { it.getLong(0) }
        }

        assertThat(start[Calendar.YEAR], `is`(2026))
        assertThat(start[Calendar.MONTH] + 1, `is`(8))
        assertThat(start[Calendar.DAY_OF_MONTH], `is`(23))
        assertThat(start[Calendar.HOUR_OF_DAY], `is`(0))
    }

    // ---------- przypomnienie ----------

    @Test
    fun przypomnienie_wypada_o_zadanej_godzinie_czasu_lokalnego() {
        TimeZone.setDefault(TimeZone.getTimeZone(STREFA_WYDARZENIA))
        val id = zapiszDzien(
            przypomnienie = SimpleReminder(
                SimpleReminder.METHOD_ALERT, SimpleTime("10:00"), 1, STREFA_WYDARZENIA
            )
        )

        val minuty = context.contentResolver.query(
            CalendarContract.Reminders.CONTENT_URI,
            arrayOf(CalendarContract.Reminders.MINUTES),
            "${CalendarContract.Reminders.EVENT_ID} = ?", arrayOf(id.toString()), null
        )!!.use { kursor ->
            assertThat("przypomnienie nie zostało zapisane", kursor.moveToFirst(), `is`(true))
            kursor.getLong(0)
        }

        // Sierpień, czyli czas letni. Od 10:00 czasu polskiego 22.08 (08:00 UTC)
        // do północy UTC 23.08 mija 16 godzin. Przed poprawką było 840 minut,
        // czyli alarm o 12:00 czasu polskiego.
        assertThat(minuty, `is`(960L))

        val start = odczytaj(id, CalendarContract.Events.DTSTART) { it.getLong(0) }
        val alarm = Calendar.getInstance(TimeZone.getTimeZone(STREFA_WYDARZENIA)).apply {
            timeInMillis = start - minuty * 60_000
        }
        assertThat(alarm[Calendar.HOUR_OF_DAY], `is`(10))
        assertThat(alarm[Calendar.DAY_OF_MONTH], `is`(22))
    }

    // ---------- wydarzenie z godziną ----------

    @Test
    fun wydarzenie_z_godzina_zachowuje_strefe_polska() {
        val id = SimpleCalendarEvent.Builder(
            idKalendarza.toString(), SimpleDate(2026, 4, 4), SimpleTime("13:00"),
            STREFA_WYDARZENIA, false
        ).setTitle("Wielka Sobota").build().write(context)

        assertThat(
            odczytaj(id, CalendarContract.Events.EVENT_TIMEZONE) { it.getString(0) },
            `is`(STREFA_WYDARZENIA)
        )
        // Kwiecień, czas letni: 13:00 w Warszawie to 11:00 UTC.
        val start = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = odczytaj(id, CalendarContract.Events.DTSTART) { it.getLong(0) }
        }
        assertThat(start[Calendar.HOUR_OF_DAY], `is`(11))
    }

    // ---------- istnienie i usuwanie ----------

    @Test
    fun zapisane_wydarzenie_jest_widoczne_i_daje_sie_usunac() {
        val id = zapiszDzien()

        assertThat(SimpleCalendarEvent.exists(context, id), `is`(true))
        assertThat(SimpleCalendarEvent.delete(id, context, idKalendarza.toString()), `is`(true))
        assertThat(SimpleCalendarEvent.exists(context, id), `is`(false))
    }
}
