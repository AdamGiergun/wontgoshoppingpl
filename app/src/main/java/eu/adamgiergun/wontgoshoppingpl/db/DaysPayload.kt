package eu.adamgiergun.wontgoshoppingpl.db

import android.content.Context
import android.util.Log
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day
import org.json.JSONObject

/**
 * Dane o dniach handlowych i niehandlowych. Ten sam format obowiązuje w
 * `res/raw/days.json` (wartość domyślna, wbudowana w aplikację) i w parametrze
 * Firebase Remote Config, dzięki czemu dodanie kolejnego roku nie wymaga wydania:
 *
 * ```
 * { "2027": { "nonCommerceDays": [ { "date": "2027_01_03", "time": "00:00",
 *                                    "key": "non_commerce_sunday" } ],
 *             "commerceSundays": [ ... ],
 *             "holidays":        [ ... ] } }
 * ```
 *
 * Pole `key` to klucz nazwy, nie gotowy tekst — patrz
 * [eu.adamgiergun.wontgoshoppingpl.day.DayNames].
 */
internal object DaysPayload {

    private const val TAG = "WGS_Days"

    const val REMOTE_CONFIG_KEY = "days_data"

    private val groups = mapOf(
        "nonCommerceDays" to Day.Type.NON_COMMERCE_DAY,
        "commerceSundays" to Day.Type.COMMERCE_SUNDAY,
        "holidays" to Day.Type.HOLIDAY,
    )

    fun fromResources(context: Context): List<Event> =
        context.resources.openRawResource(R.raw.days).bufferedReader().use { reader ->
            parse(reader.readText())
        }

    /**
     * Zwraca pustą listę zamiast rzucać wyjątkiem: uszkodzony payload ze zdalnego
     * źródła nie może wywrócić aplikacji, a wtedy zostają dane już zapisane w bazie.
     */
    fun parse(json: String): List<Event> {
        return runCatching {
            val root = JSONObject(json)
            buildList {
                for (year in root.keys()) {
                    val yearObject = root.getJSONObject(year)
                    for ((group, type) in groups) {
                        val array = yearObject.optJSONArray(group) ?: continue
                        for (i in 0 until array.length()) {
                            val entry = array.getJSONObject(i)
                            add(
                                Event(
                                    null,
                                    type.code,
                                    "${entry.getString("date")}," +
                                            "${entry.getString("time")}," +
                                            entry.getString("key"),
                                    null,
                                    null
                                )
                            )
                        }
                    }
                }
            }.sortedBy { it.item }
        }.getOrElse {
            Log.w(TAG, "Nieprawidłowy payload z datami: ${it.message}")
            emptyList()
        }
    }
}
