package eu.adamgiergun.wontgoshoppingpl.main.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.AdView
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithThemeAndPermissions
import eu.adamgiergun.wontgoshoppingpl.common.AdsContainer
import eu.adamgiergun.wontgoshoppingpl.common.DaysRepository
import eu.adamgiergun.wontgoshoppingpl.databinding.ActivityMainBinding
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.MainViewModel
import eu.adamgiergun.wontgoshoppingpl.main.fragments.WidgetInfoFragmentDirections
import kotlinx.coroutines.launch

/*
    TODO "remove all events from calendar" in Settings
    TODO "Info" group in Settings(?)
    TODO move events filtering from settings to app bar(?)
*/
internal class MainActivity : ActivityWithThemeAndPermissions() {

    private val mainViewModel: MainViewModel by lazy { mViewModel as MainViewModel }
    private var navController: NavController? = null
    private var bannerAdView: AdView? = null
    //    private var calStart: Calendar

    //region ActivityLifeCycleRegion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        calStart = Calendar.getInstance();
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarInsets(binding.root)
        mainViewModel.apply {
            navController = (supportFragmentManager
                .findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment)
                .navController
                .also { it.setGraph(mainNavigationId) }

            allPermissionsGranted.observe(this@MainActivity) {
                if (it) {
                    when (nextAction.value) {
                        Actions.CHECK_PERMISSIONS_LIST ->
                            setNextAction(Actions.CHECK_CALENDAR_SETTINGS_LIST)
                        Actions.CHECK_PERMISSIONS_DAY ->
                            setNextAction(Actions.CHECK_CALENDAR_SETTINGS_DAY)
                        else -> Unit
                    }
                } else {
                    setNextAction(Actions.NONE)
                }
            }

            isWidgetInfoButtonClicked.observe(this@MainActivity) {
                if (!it) return@observe
                navController?.navigate(WidgetInfoFragmentDirections.actionWidgetInfoFragmentToDaysFragment())
                setAppWidgetInfoDisplayed()
                // Odnotowanie pierwszego uruchomienia przeniosło się tutaj z ekranu
                // wyboru wyglądu, który już nie istnieje.
                onFirstRunDone()
                invalidateOptionsMenu()
                onWidgetInfoNavigationDone()
            }

            nextAction.observe(this@MainActivity) {
                @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
                when (it) {
                    Actions.CHECK_PERMISSIONS_DAY -> checkCalendarPermissions(Actions.CHECK_CALENDAR_SETTINGS_DAY)
                    Actions.CHECK_PERMISSIONS_LIST -> checkCalendarPermissions(Actions.CHECK_CALENDAR_SETTINGS_LIST)
                    Actions.CHECK_CALENDAR_SETTINGS_DAY -> checkCalendarSettings(Actions.ASK_FOR_REMINDER_SETTINGS)
                    Actions.CHECK_CALENDAR_SETTINGS_LIST -> checkCalendarSettings(Actions.ASK_FOR_DAY_TYPES_ADDED_TO_CALENDAR)
                    // Trzy akcje pytające pokazuje warstwa Compose ekranu listy.
                    // Akcja MUSI tu zostać niezmieniona: to ona jest stanem
                    // widoczności dialogu, a zerowanie należy do tego, kto go
                    // zamyka. Wcześniej aktywność pokazywała dialog i zaraz
                    // ustawiała NONE, bo widoczność trzymał menedżer fragmentów.
                    Actions.ASK_FOR_REMINDER_SETTINGS,
                    Actions.ASK_FOR_DAY_TYPES_ADDED_TO_CALENDAR,
                    Actions.ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL -> Unit
                    Actions.START_SETTINGS_ACTIVITY_IN_NOTIFICATIONS -> {
                        startActivity(getSettingsActivityIntentAtNotifications())
                        setNextAction(Actions.NONE)
                    }
                    Actions.NONE -> Unit
                }
            }

            lifecycleScope.launch {
                bannerAdView = AdsContainer.start(this@MainActivity, binding.adViewLayout)
            }

            lifecycleScope.launch {
                DaysRepository.refresh(applicationContext)
            }
//        addCrashTestButton();
        }
    }

//        private void addCrashTestButton() {
//            Button crashButton = new Button(this);
//            crashButton.setText("Crash!");
//            crashButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View view) {
//                    throw new RuntimeException("Test Crash"); // Force a crash
//                }
//            });
//
//            addContentView(crashButton, new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//        }

    //    private void showStartTime() {
    //        val calEnd = Calendar.getInstance()
    //        val timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
    //        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();
    //    }

    override fun onResume() {
        super.onResume()
        bannerAdView?.resume()
        if (navController == null) navController = findNavController(R.id.main_nav_host_fragment)
    }

    override fun onPause() {
        bannerAdView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        // Baner jest tworzony w kodzie przy każdym odtworzeniu aktywności (obrót),
        // więc trzeba go jawnie zwolnić.
        bannerAdView?.destroy()
        bannerAdView = null
        super.onDestroy()
    }
    //endregion

    //region AppMenuHandlingRegion
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuLayoutId =
            if (mainViewModel.wasWidgetInfoDisplayed) R.menu.main_activity_actions else R.menu.main_activity_actions_first_run
        menuInflater.inflate(menuLayoutId, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                mainViewModel.startSettingsActivity(this)
                true
            }
            R.id.calendar -> {
                mainViewModel.setNextAction(Actions.CHECK_PERMISSIONS_LIST)
                true
            }
            R.id.help -> {
                mainViewModel.startHelpActivity(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkCalendarPermissions(nextActions: Actions) {
        if (mViewModel.allPermissionsGranted.value == true) mainViewModel.setNextAction(nextActions)
        else requestCalendarPermissions()
    }

    private fun checkCalendarSettings(nextActions: Actions) {
        mainViewModel.setNextAction(
            if (mainViewModel.isCalendarEnabledAndChosen)
                nextActions
            else
                Actions.START_SETTINGS_ACTIVITY_IN_NOTIFICATIONS
        )
    }

    //endregion

    enum class Actions {
        CHECK_PERMISSIONS_DAY,
        CHECK_CALENDAR_SETTINGS_DAY,
        ASK_FOR_REMINDER_SETTINGS,
        CHECK_PERMISSIONS_LIST,
        CHECK_CALENDAR_SETTINGS_LIST,
        ASK_FOR_DAY_TYPES_ADDED_TO_CALENDAR,
        ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL,
        START_SETTINGS_ACTIVITY_IN_NOTIFICATIONS,
        NONE
    }
}