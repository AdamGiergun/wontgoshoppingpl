package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithThemeAndPermissions
import eu.adamgiergun.wontgoshoppingpl.common.InterstitialAdContainer
import eu.adamgiergun.wontgoshoppingpl.common.isLookChanged
import eu.adamgiergun.wontgoshoppingpl.databinding.ActivityMainBinding
import eu.adamgiergun.wontgoshoppingpl.help.HelpActivity
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity

/*
    TODO "remove all events from calendar" in Settings
    TODO "Info" group in Settings(?)
    TODO move events filtering from settings to app bar(?)
*/
internal class MainActivity : ActivityWithThemeAndPermissions() {

    private val mMainViewModel: MainViewModel by lazy {
        mViewModel as MainViewModel
    }

    //    private var calStart: Calendar
    //region ActivityLifeCycleRegion
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        calStart = Calendar.getInstance();
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mMainViewModel.let {
            binding.viewModel = it
            it.askForReminderSettings.observe(this, { ask ->
                if (ask) askForReminderSettings()
            })

            it.lastTouchedDay.observe(this, {
                checkCalendarPermissions(checkCalendarSettings)
            })
        }

        lifecycleScope.launchWhenStarted {
            setupBannerAds(binding.adView)
            InterstitialAdContainer.initializeAd(applicationContext)
        }
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

    private fun setupBannerAds(mAdView: AdView) {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this@MainActivity)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    //    private void showStartTime() {
    //        val calEnd = Calendar.getInstance()
    //        val timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
    //        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();
    //    }
    //endregion

    override fun onResume() {
        super.onResume()
        if (isLookChanged) {
            isLookChanged = false
            mMainViewModel.resetDayTypes()
            recreate()
        }
    }

    //region AppMenuHandlingRegion
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuLayoutId = if (mMainViewModel.wasWidgetInfoDisplayed) R.menu.main_activity_actions else R.menu.main_activity_actions_first_run
        menuInflater.inflate(menuLayoutId, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                true
            }
            R.id.calendar -> {
                mMainViewModel.setActionAddList()
                checkCalendarPermissions(checkCalendarSettings)
                true
            }
            R.id.help -> {
                startActivity(Intent(applicationContext, HelpActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val checkCalendarSettings = {
        if (mMainViewModel.isCalendarEnabledAndChosen) {
            if (mMainViewModel.isActionAddList) {
                askForDayTypesAddedToCalendar()
            } else {
                askForReminderSettings()
            }
        } else {
            startActivity(mMainViewModel.getSettingsActivityIntentAtNotifications())
        }
    }

    private fun askForReminderSettings() {
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
    //endregion

    override fun onCalendarPermissionsGranted() {
        checkCalendarSettings()
    }

    //region on...ClickRegion
    fun onLookButtonClick(view: View) {
        view.findNavController().navigate(AskForLookFragmentDirections.actionAskForLookFragmentToWidgetInfoFragment())
        (view.id == R.id.imageButtonRich).let { isLookRich ->
            mMainViewModel.onInitialLookChoice(isLookRich)
            if (isLookRich)
                recreate()
        }
    }

    fun onWidgetInfoButtonClick(view: View) {
        view.findNavController().navigate(WidgetInfoFragmentDirections.actionWidgetInfoFragmentToDaysFragment())
        mMainViewModel.setAppWidgetInfoDisplayed()
        invalidateOptionsMenu()
    }
    //endregion
}