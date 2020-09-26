package eu.adamgiergun.wontgoshoppingpl.main


import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.mylibrary.NoticeDialogFragment.NoticeDialogListener
import eu.adamgiergun.mylibrary.NoticeDialogFragment.NoticeDialogViewListener
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithTheme
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst
import eu.adamgiergun.wontgoshoppingpl.databinding.ActivityMainBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.help.HelpActivity
import eu.adamgiergun.wontgoshoppingpl.main.DaysFragment.OnItemClickListener
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity
import kotlinx.coroutines.launch
import java.util.*

/*
    TODO "remove all events from calendar" in Settings
    TODO "Info" group in Settings(?)
    TODO move events filtering from settings to app bar(?)
*/
class MainActivity : ActivityWithTheme(), NoticeDialogListener, OnItemClickListener, NoticeDialogViewListener {

    private var interstitialAd: InterstitialAd? = null

    private val mMainViewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    //    private var calStart: Calendar
    //region ActivityLifeCycleRegion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        calStart = Calendar.getInstance();
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vieModel = mMainViewModel
        showWidgetInfoIfNeeded()
        setupBannerAds(binding.adView)
        initializeOrLoadInterstitialAd(applicationContext)
        if (savedInstanceState == null) {
            setLocalizedConst()
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

    private fun setLocalizedConst() {
        if (!LocalizedConst.isSet) {
            LocalizedConst.wholeDay = getString(R.string.whole_day)
            LocalizedConst.since = getString(R.string.since)
            LocalizedConst.askForLook = getString(R.string.ask_for_look)
            LocalizedConst.isSet = true
        }
    }

    private fun showWidgetInfoIfNeeded() {
        if (!mMainViewModel.wasAppWidgetInfoDisplayed()) {
            WidgetInfoDialog
                    .newInstance()
                    .show(supportFragmentManager, "")
        }
    }

    override fun setDefaultLocale(context: Context?): Context? {
        LocalizedConst.locale = Locale.getDefault()
        return super.setDefaultLocale(context)
    }

    //region Ads Handling region
    private fun setupBannerAds(mAdView: AdView) {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        lifecycleScope.launch {
            MobileAds.initialize(this@MainActivity)
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)
        }
    }

    private fun initializeOrLoadInterstitialAd(context: Context) {
        lifecycleScope.launch {
            if (interstitialAd == null) {
                interstitialAd = InterstitialAd(context)
                interstitialAd!!.adUnitId = context.getString(R.string.interstitial_ad_id)
            }
            loadInterstitialAd()
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        interstitialAd!!.loadAd(adRequest)
    }

    private fun getInterstitialAd(): InterstitialAd? {
        return if (interstitialAd == null) {
            initializeOrLoadInterstitialAd(applicationContext)
            null
        } else {
            if (interstitialAd!!.isLoaded) {
                interstitialAd
            } else {
                if (!interstitialAd!!.isLoading) {
                    loadInterstitialAd()
                }
                null
            }
        }
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
        mMainViewModel.resetReminderToDefaultValue()
        if (mMainViewModel.isActionAddList) {
            ReminderSettingsAndDateTillDialog()
                    .show(supportFragmentManager, "")
        } else {
            ReminderSettingsDialog()
                    .show(supportFragmentManager, "")
        }
    }

    private fun askForDayTypesAddedToCalendar() {
        DayTypesAddedToCalendarDialog
                .newInstance()
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

    //region ShowingInfoRegion
    private fun showInfo(resultIsPositive: Boolean, positiveInfo: Int, negativeInfo: Int) {
        val info = if (resultIsPositive) positiveInfo else negativeInfo
        showInfo(info)
    }

    private fun showInfo(info: Int) {
        Toast.makeText(applicationContext, info, Toast.LENGTH_SHORT).show()
    }

    //endregion
    override fun onCalendarPermissionsGranted() {
        checkCalendarSettings()
    }

    //region on...ClickRegion
    override fun onDialogPositiveClick(dialog: NoticeDialogFragment) {
        if (dialog is DayTypesAddedToCalendarDialog) {
            askForReminderSettings()
        } else if (dialog is ReminderSettingsDialog || dialog is ReminderSettingsAndDateTillDialog) {
            lifecycleScope.launch {
                addToCalendar()
            }
        }
    }

    private suspend fun addToCalendar() {
        lifecycleScope.launch {
            getInterstitialAd()?.show()
            val isResultPositive = mMainViewModel.addToCalendar()
            showInfo(isResultPositive, R.string.info_added_to_calendar, mMainViewModel.error)
        }
    }

    override fun onDialogNegativeClick(dialog: NoticeDialogFragment) {
        if (dialog is DayTypesAddedToCalendarDialog || dialog is ReminderSettingsDialog || dialog is ReminderSettingsAndDateTillDialog) {
            showInfo(R.string.info_cancelled)
        }
    }

    override fun onViewClick(noticeDialogFragment: NoticeDialogFragment) {
        noticeDialogFragment.show(supportFragmentManager, "")
    }

    fun onLookButtonClick(view: View) {
        val isLookRich = view.id == R.id.imageButtonRich
        mMainViewModel.onInitialLookChoice(isLookRich)
        view.findNavController().navigate(AskForLookDirections.actionAskForLookToDaysFragment())
        invalidateOptionsMenu()
    }
    //endregion
}