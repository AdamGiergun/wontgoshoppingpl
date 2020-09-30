package eu.adamgiergun.wontgoshoppingpl.common

import android.Manifest.permission
import android.content.pm.PackageManager
import android.content.res.Resources.Theme
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.mykotlinlibrary.AppCompatActivityWithHomeInBar
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.areAllPermissionsGranted
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissions
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissionsCode

internal abstract class ActivityWithThemeAndPermissions : AppCompatActivityWithHomeInBar() {

    private val mViewModel by lazy {
        ViewModelProvider(this).get(AppViewModel::class.java)
    }

    override fun getTheme(): Theme {
        return super.getTheme().apply {
            val style = if (mViewModel.isLookRich) R.style.AppThemeCustomizationsRich else R.style.AppThemeCustomizationsMini
            applyStyle(style, true)
        }
    }

    protected fun areCalendarPermissionsGranted(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                isGranted(permission.READ_CALENDAR)
                && isGranted(permission.WRITE_CALENDAR)
    }

    private fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected fun checkCalendarPermissions(task: () -> Unit) {
        when {
            areCalendarPermissionsGranted() -> task()
            shouldShowRequestPermissionRationale(permission.WRITE_CALENDAR) -> showRationaleAndRequestCalendarPermissions()
            else -> requestCalendarPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    protected fun showRationaleAndRequestCalendarPermissions() {
        showRationale()
        requestCalendarPermissions()
    }

    private fun showRationale() {
        Toast.makeText(applicationContext, R.string.info_explain_calendar_permission, Toast.LENGTH_LONG).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected fun requestCalendarPermissions() {
        requestPermissions(calendarPermissions, calendarPermissionsCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == calendarPermissionsCode) {
            if (areAllPermissionsGranted(grantResults)) {
                onCalendarPermissionsGranted()
            }
        }
    }

    protected open fun onCalendarPermissionsGranted() {}

    protected open fun onCalendarPermissionNotGranted() {
        showRationale()
    }
}