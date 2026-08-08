package eu.adamgiergun.wontgoshoppingpl.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import eu.adamgiergun.wontgoshoppingpl.R

class CalendarsListPreference : ListPreference {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private val idForNoCalendarSet
        get() = context.getString(R.string.id_for_default_calendar_not_set)
    private val idForNoCalendarsAvailable
        get() = context.getString(R.string.id_for_no_calendars_available)
    private val isSystemAtLeastJellyBeanMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
    private lateinit var singleCalendarId: String
    private lateinit var primaryCalendarId: String
    private var noCalendarsAvailable = false

    init {
        if (areCalendarPermissionsGranted(context.applicationContext)) {
            cursor.use { cursor ->
                when {
                    cursor == null -> setPreferenceWithNoCalendarsAvailable()
                    cursor.count == 0 -> setPreferenceWithNoCalendarsAvailable()
                    cursor.count == 1 -> setPreferenceWithSingleCalendar(cursor)
                    else -> setPreferenceWithCalendars(cursor)
                }
            }
        } else {
            setPreferenceWithNoCalendarsAvailable()
        }
    }

    private val cursor
        get() = context.contentResolver.run {
            val listOfFields = if (isSystemAtLeastJellyBeanMR1) {
                arrayOf("_id", "calendar_displayName", "isPrimary")
            } else {
                arrayOf("_id", "calendar_displayName")
            }
            val selectionArgs = arrayOf("700")
            this.query(
                    Uri.parse("content://com.android.calendar/calendars"),
                    listOfFields,
                    "calendar_access_level=?",
                    selectionArgs,
                    "calendar_displayName")
        }

    private fun setPreferenceWithNoCalendarsAvailable() {
        noCalendarsAvailable = true
        isEnabled = false
        entries = arrayOf(context.getString(R.string.no_calendars_available))
        entryValues = arrayOf(idForNoCalendarsAvailable)
    }

    private fun setPreferenceWithSingleCalendar(cursor: Cursor) {
        cursor.run {
            moveToFirst()
            entries = arrayOf(getString(1))
            getInt(0).toString().let { id ->
                entryValues = arrayOf(id)
                singleCalendarId = id
            }
        }
    }

    private fun setPreferenceWithCalendars(cursor: Cursor) {
        primaryCalendarId = idForNoCalendarSet
        val mEntries = ArrayList<CharSequence>()
        val mEntryValues = ArrayList<CharSequence>()
        cursor.run {
            while (moveToNext()) {
                getString(1).let { name -> mEntries.add(name) }
                getInt(0).toString().let { id ->
                    mEntryValues.add(id)
                    if (isSystemAtLeastJellyBeanMR1 && getInt(2) == 1) primaryCalendarId = id
                }
            }
        }
        entries = mEntries.toTypedArray()
        entryValues = mEntryValues.toTypedArray()
    }

    override fun onAttached() {
        super.onAttached()
        if (::singleCalendarId.isInitialized)
            persistString(singleCalendarId)
        else
            usePrimaryCalendarIfNoCalendarSet()
    }

    private fun usePrimaryCalendarIfNoCalendarSet() {
        if (value.toInt() == -1)
            when {
                ::primaryCalendarId.isInitialized ->
                    persistString(primaryCalendarId)
                noCalendarsAvailable -> {
                    persistString(idForNoCalendarsAvailable)
                    entryValues.plus(idForNoCalendarsAvailable)
                    entries.plus(context.getString(R.string.no_calendars_available))
                }
                else -> {
                    persistString(idForNoCalendarSet)
                    entryValues.plus(idForNoCalendarSet)
                    entries.plus(context.getString(R.string.calendar_not_set))
                }
            }
    }
}

fun areCalendarPermissionsGranted(appContext: Context): Boolean {
    fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED
    }
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            isGranted(Manifest.permission.READ_CALENDAR)
            && isGranted(Manifest.permission.WRITE_CALENDAR)
}