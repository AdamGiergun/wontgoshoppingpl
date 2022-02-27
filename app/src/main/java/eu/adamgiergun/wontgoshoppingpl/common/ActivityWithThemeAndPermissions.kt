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
import eu.adamgiergun.wontgoshoppingpl.help.HelpViewModel
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.MainViewModel
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsViewModel

internal abstract class ActivityWithThemeAndPermissions : AppCompatActivityWithHomeInBar() {

    protected val mViewModel by lazy {
        when (this) {
            is MainActivity -> ViewModelProvider(this)[MainViewModel::class.java]
            is SettingsActivity -> ViewModelProvider(this)[SettingsViewModel::class.java]
            else -> ViewModelProvider(this)[HelpViewModel::class.java]
        }
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
                mViewModel.setAllPermissionsGranted(true)
            } else {
                mViewModel.setAllPermissionsGranted(false)
                Toast.makeText(this, R.string.info_explain_calendar_permission, Toast.LENGTH_LONG).show()
            }
        }
    }
}