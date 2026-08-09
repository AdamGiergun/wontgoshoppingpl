package eu.adamgiergun.wontgoshoppingpl.common

import android.graphics.Rect
import android.os.Bundle
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

    /**
     * Odsuwa treść od pasków systemowych i wcięcia ekranu.
     *
     * Górnego insetu nie trzeba niczym uzupełniać: ActionBarOverlayLayout
     * przekazuje w dół inset powiększony o wysokość paska akcji. Wcześniej
     * dokładaliśmy tu jeszcze actionBarSize i pasek zostawiał pod sobą pustą
     * wstęgę wysokości całego paska.
     *
     * Inset **dodajemy** do wypełnienia zapisanego w layoucie, a nie wstawiamy
     * w jego miejsce. Wersja podmieniająca kasowała wypełnienie z XML-a: ekran
     * pomocy deklarował 16 dp marginesu na tekst, a nasłuch ustawiał je na
     * wartość insetu, czyli na urządzeniu bez wcięć na zero — tekst dotykał
     * krawędzi ekranu. Wartość początkową trzeba zapamiętać, bo nasłuch bywa
     * wołany wielokrotnie i za drugim razem czytałby własny wynik.
     */
    protected fun applySystemBarInsets(view: View) {
        val wyjsciowe = Rect(
            view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
        )
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = wyjsciowe.left + insets.left,
                top = wyjsciowe.top + insets.top,
                right = wyjsciowe.right + insets.right,
                bottom = wyjsciowe.bottom + insets.bottom
            )
            windowInsets
        }
    }

    protected fun requestCalendarPermissions() {
        calendarPermissionsLauncher.launch(calendarPermissions)
    }
}
