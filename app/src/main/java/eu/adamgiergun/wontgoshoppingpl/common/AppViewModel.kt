package eu.adamgiergun.wontgoshoppingpl.common

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.adamgiergun.mykotlinlibrary.PermissionType

internal abstract class AppViewModel(application: Application) : AndroidViewModel(application) {

    protected val appContext: Context
        get() = getApplication<Application>().applicationContext

    var isLookRich: Boolean
        get() = AppPreferences.isLookRich(appContext)
        protected set(isLookRich) {
            AppPreferences.setLookRich(appContext, isLookRich)
        }

    private var _allPermissionsGranted = MutableLiveData<Boolean>()
    val allPermissionsGranted: LiveData<Boolean>
        get() = _allPermissionsGranted

    fun setAllPermissionsGranted(value: Boolean) {
        _allPermissionsGranted.value = value
    }

    private var _askingForPermissionsNeeded = MutableLiveData<Boolean>()
    val askingForPermissionsNeeded: LiveData<Boolean>
        get() = _askingForPermissionsNeeded

    fun askForPermissions() {
        _askingForPermissionsNeeded.value = true
    }

    init {
        _allPermissionsGranted.value =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                        || isGranted(Manifest.permission.READ_CALENDAR)
                        && isGranted(Manifest.permission.WRITE_CALENDAR)
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED
    }

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