package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithTheme
import eu.adamgiergun.wontgoshoppingpl.common.InterstitialAdContainer
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst
import eu.adamgiergun.wontgoshoppingpl.databinding.ActivityMainBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.help.HelpActivity
import eu.adamgiergun.wontgoshoppingpl.main.DaysFragment.OnItemClickListener
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity
import java.util.*

/*
    TODO "remove all events from calendar" in Settings
    TODO "Info" group in Settings(?)
    TODO move events filtering from settings to app bar(?)
*/
internal class MainActivity : ActivityWithTheme(), OnItemClickListener {

    private val mMainViewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    //    private var calStart: Calendar
    //region ActivityLifeCycleRegion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        calStart = Calendar.getInstance();
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = mMainViewModel

        lifecycleScope.launchWhenStarted {
            setupBannerAds(binding.adView)
            InterstitialAdContainer.initializeAd(applicationContext)
        }

        mMainViewModel.askForReminderSettings.observe(this, {
            if (it) askForReminderSettings()
        })

//        addCrashTestButton();
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

    override fun setDefaultLocale(context: Context?): Context? {
        LocalizedConst.locale = Locale.getDefault()
        return super.setDefaultLocale(context)
    }

    //region Ads Handling region
    private fun setupBannerAds(mAdView: AdView) {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this@MainActivity)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
    //endregion

    //    private void showStartTime() {
    //        val calEnd = Calendar.getInstance()
    //        val timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
    //        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();
    //    }
    //endregion

    //region AppMenuHandlingRegion
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuLayoutId = if (mMainViewModel.isFirstRun) R.menu.main_activity_actions_first_run else R.menu.main_activity_actions
        menuInflater.inflate(menuLayoutId, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                startSettingsActivity(false)
                true
            }
            R.id.calendar -> {
                mMainViewModel.setActionAddList()
                checkCalendarPermissions()
                true
            }
            R.id.help -> {
                startHelpActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startSettingsActivity(startFromNotifications: Boolean) {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        intent.putExtra(
                MainViewModel.START_FRAGMENT_ID_TAG,
                MainViewModel.getStartFragmentId(startFromNotifications))
        startActivity(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkCalendarPermissions() {
        if (mMainViewModel.areCalendarPermissionsGranted()) {
            checkCalendarSettings()
        } else {
            requestCalendarPermissions()
        }
    }

    private fun checkCalendarSettings() {
        if (mMainViewModel.isCalendarEnabledAndChosen) {
            if (mMainViewModel.isActionAddList) {
                askForDayTypesAddedToCalendar()
            } else {
                askForReminderSettings()
            }
        } else {
            startSettingsActivity(true)
        }
    }

    private fun askForReminderSettings() {
        mMainViewModel.setReminderToDefaultValueIfNull()
        if (mMainViewModel.isActionAddList) {
            ReminderSettingsAndDateTillDialog()
                    .show(supportFragmentManager, "")
        } else {
            ReminderSettingsDialog()
                    .show(supportFragmentManager, "")
        }
    }

    private fun askForDayTypesAddedToCalendar() {
        DayTypesAddedToCalendarDialog()
                .show(supportFragmentManager, "dialog")
    }

    private fun startHelpActivity() {
        val intent = Intent(applicationContext, HelpActivity::class.java)
        startActivity(intent)
    }

    //endregion
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onItemClick(day: Day) {
        mMainViewModel.setLastTouchedDay(day)
        checkCalendarPermissions()
    }

    override fun onCalendarPermissionsGranted() {
        checkCalendarSettings()
    }

    //region on...ClickRegion
    fun onLookButtonClick(view: View) {
        (view.id == R.id.imageButtonRich).let { isLookRich ->
            mMainViewModel.onInitialLookChoice(isLookRich)
            if (isLookRich)
                recreate()
        }
        view.findNavController().navigate(AskForLookFragmentDirections.actionAskForLookFragmentToWidgetInfoFragment())
    }

    fun onWidgetInfoButtonClick(view: View) {
        view.findNavController().navigate(WidgetInfoFragmentDirections.actionWidgetInfoFragmentToDaysFragment())
        mMainViewModel.setAppWidgetInfoDisplayed()
        invalidateOptionsMenu()
    }
    //endregion
}