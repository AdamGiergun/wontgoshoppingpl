package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources.Theme
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.mylibrary.AppCompatActivityWithHomeInBar
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.areAllPermissionsGranted
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissions
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissionsCode

abstract class ActivityWithTheme : AppCompatActivityWithHomeInBar() {
    private var mViewModel: AppViewModel? = null
    override fun getTheme(): Theme {
        val theme = super.getTheme()
        if (mViewModel == null) {
            mViewModel = ViewModelProvider(this).get(AppViewModel::class.java)
            val style = if (mViewModel!!.isLookRich) R.style.AppThemeCustomizationsRich else R.style.AppThemeCustomizationsMini
            theme.applyStyle(style, true)
        }
        return theme
    }

    override fun attachBaseContext(newBase: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            super.attachBaseContext(newBase)
            setDefaultLocale(newBase)
        } else {
            super.attachBaseContext(setDefaultLocale(newBase))
        }
    }

    protected open fun setDefaultLocale(context: Context?): Context? {
        val config = Configuration()
        if (LocalizedConst.locale != null) setLocale(config)
        return updateConfiguration(context, config)
    }

    private fun setLocale(config: Configuration) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            @Suppress("DEPRECATION")
            config.locale = LocalizedConst.locale
        } else {
            config.setLocale(LocalizedConst.locale)
        }
    }

    private fun updateConfiguration(context: Context?, config: Configuration): Context? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(
                    config,
                    resources.displayMetrics)
            null
        } else {
            context!!.createConfigurationContext(config)
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
        Toast.makeText(applicationContext, eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission, Toast.LENGTH_LONG).show()
    }

    protected fun removeAllFragments() {
        val fragmentManager = supportFragmentManager
        val fragments = fragmentManager.fragments
        val fragmentTransaction = fragmentManager.beginTransaction()
        for (fragment in fragments) fragmentTransaction.remove(fragment!!)
        fragmentTransaction.commit()
        supportFragmentManager.executePendingTransactions()
    }
}