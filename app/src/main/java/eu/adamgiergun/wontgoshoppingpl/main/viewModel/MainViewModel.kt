package eu.adamgiergun.wontgoshoppingpl.main.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.*
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.objects.MainActivityActions
import eu.adamgiergun.wontgoshoppingpl.settings.NotificationPreferenceFragment
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity
import kotlinx.coroutines.launch

internal class MainViewModel(application: Application) : AppViewModel(application) {

    private val _isWidgetInfoButtonClicked = MutableLiveData<Boolean>()
    val isWidgetInfoButtonClicked : LiveData<Boolean>
        get() = _isWidgetInfoButtonClicked

    fun onWidgetInfoButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        _isWidgetInfoButtonClicked.value = true
    }

    /**
     * Zdarzenie jednorazowe: bez wyzerowania LiveData poda tę samą wartość ponownie
     * przy odtworzeniu aktywności (np. po obrocie), a nawigacja z widgetInfoFragment
     * nie istnieje już z bieżącego miejsca w grafie.
     */
    fun onWidgetInfoNavigationDone() {
        _isWidgetInfoButtonClicked.value = false
    }

    private val isFirstRun: Boolean
        get() = AppPreferences.isFirstRun(appContext)

    init {
        if (isFirstRun)
            viewModelScope.launch { AppDatabase.getInstance(getApplication()).eventDao().initialize() }
    }

    val nextAction: LiveData<MainActivity.Actions>
        get() = MainActivityActions.nextAction
    fun setNextAction(action: MainActivity.Actions) {
        MainActivityActions.setNextAction(action)
    }

    val isCalendarEnabledAndChosen: Boolean
        get() = AppPreferences.isUseGoogleCalendarSet(appContext) && AppPreferences.isCalendarSet(appContext)

    // Pierwsze uruchomienie i „widget jeszcze nieopisany" prowadzą dziś do tego
    // samego miejsca — ekran wyboru wyglądu, który je rozdzielał, zniknął razem
    // z parą rich/mini.
    val mainNavigationId: Int
        get() = if (isFirstRun || !AppPreferences.wasAppWidgetInfoDisplayed(appContext))
            R.navigation.main_navigation_widget_info
        else
            R.navigation.main_navigation

    val wasWidgetInfoDisplayed
        get() = AppPreferences.wasAppWidgetInfoDisplayed(appContext)

    fun getSettingsActivityIntentAtNotifications(): Intent {
        return Intent(appContext, SettingsActivity::class.java).apply {
            putExtra(START_FRAGMENT_ID_TAG, NotificationPreferenceFragment::class.java.name)
        }
    }

    /**
     * Pierwsze uruchomienie nie pyta już o wygląd — została jedna, spójna szata
     * graficzna z trybem jasnym i ciemnym. Zostaje samo odnotowanie faktu.
     */
    fun onFirstRunDone() {
        setNotFirstRun()
    }

    fun setAppWidgetInfoDisplayed() {
        AppPreferences.setAppWidgetInfoDisplayed(appContext)
    }

    private fun setNotFirstRun() {
        AppPreferences.setFirstRun(appContext, false)
    }
}