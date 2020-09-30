package eu.adamgiergun.wontgoshoppingpl.common

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import eu.adamgiergun.mykotlinlibrary.PermissionType

internal open class AppViewModel(application: Application) : AndroidViewModel(application) {

    var isLookRich: Boolean
        get() = AppPreferences.isLookRich(appContext)
        protected set(isLookRich) {
            AppPreferences.setLookRich(appContext, isLookRich)
        }

    protected val appContext: Context
        get() = getApplication<Application>().applicationContext

    companion object {
        const val START_FRAGMENT_ID_TAG = "start_fragment_id"

        @JvmStatic
        fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
            var result = true
            for (grantResult in grantResults) {
                result = result && grantResult == PackageManager.PERMISSION_GRANTED
            }
            return result
        }

        @JvmStatic
        val calendarPermissions: Array<String>
            get() = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)

        @JvmStatic
        val calendarPermissionsCode: Int
            get() = PermissionType.CALENDAR.code
    }
}