package eu.adamgiergun.wontgoshoppingpl.common

import android.content.res.Resources.Theme
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.mykotlinlibrary.AppCompatActivityWithHomeInBar
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.areAllPermissionsGranted
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissions
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissionsCode

internal abstract class ActivityWithTheme : AppCompatActivityWithHomeInBar() {

    private val mViewModel by lazy {
        ViewModelProvider(this).get(AppViewModel::class.java)
    }

    override fun getTheme(): Theme {
        return super.getTheme().apply {
            val style = if (mViewModel.isLookRich) R.style.AppThemeCustomizationsRich else R.style.AppThemeCustomizationsMini
            applyStyle(style, true)
        }
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
            } else {
                onCalendarPermissionNotGranted()
            }
        }
    }

    protected open fun onCalendarPermissionsGranted() {}

    protected open fun onCalendarPermissionNotGranted() {
        Toast.makeText(applicationContext, R.string.info_explain_calendar_permission, Toast.LENGTH_LONG).show()
    }
}