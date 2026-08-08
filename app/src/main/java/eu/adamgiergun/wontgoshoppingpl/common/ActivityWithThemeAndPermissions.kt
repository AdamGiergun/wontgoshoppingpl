package eu.adamgiergun.wontgoshoppingpl.common

import android.content.res.Resources.Theme
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.wontgoshoppingpl.common.AppCompatActivityWithHomeInBar
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.areAllPermissionsGranted
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel.Companion.calendarPermissions
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

    private val calendarPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (areAllPermissionsGranted(results)) {
                mViewModel.setAllPermissionsGranted(true)
            } else {
                mViewModel.setAllPermissionsGranted(false)
                Toast.makeText(
                    this,
                    eu.adamgiergun.wontgoshoppingpl.R.string.info_explain_calendar_permission,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Od targetSdk 35 edge-to-edge jest wymuszone i nie da się z niego zrezygnować.
        // Musi być przed setContentView w klasach pochodnych.
        enableEdgeToEdge()
    }

    override fun getTheme(): Theme {
        return super.getTheme().apply {
            val style = if (mViewModel.isLookRich) R.style.AppThemeCustomizationsRich else R.style.AppThemeCustomizationsMini
            applyStyle(style, true)
        }
    }

    /**
     * Odsuwa treść od pasków systemowych i wcięcia ekranu. W trybie edge-to-edge
     * ActionBarOverlayLayout przestaje przesuwać treść w dół, więc do górnego insetu
     * trzeba doliczyć wysokość paska akcji — inaczej pierwszy widok chowa się pod nim.
     */
    protected fun applySystemBarInsets(view: View) {
        val actionBarHeight = TypedValue().let { typedValue ->
            if (theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, typedValue, true))
                TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
            else 0
        }
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = insets.left,
                top = insets.top + actionBarHeight,
                right = insets.right,
                bottom = insets.bottom
            )
            windowInsets
        }
    }

    protected fun requestCalendarPermissions() {
        calendarPermissionsLauncher.launch(calendarPermissions)
    }
}
