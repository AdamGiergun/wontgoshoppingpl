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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.help.HActivity
import eu.adamgiergun.wontgoshoppingpl.main.DaysFragment.onItemClickListener
import eu.adamgiergun.wontgoshoppingpl.settings.SActivity
import java.util.*

/*
    TODO "remove all events from calendar" in Settings
    TODO "Info" group in Settings(?)
    TODO move events filtering from settings to app bar(?)
*/   class Activity : ActivityWithTheme(), NoticeDialogListener, onItemClickListener, NoticeDialogViewListener {
    private var interstitialAd: InterstitialAd? = null
    private var mViewModel: ViewModel? = null

    //    private Calendar calStart;
    //region ActivityLifeCycleRegion
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        calStart = Calendar.getInstance();
        setContentView(R.layout.activity_main)
        setupBannerAds()
        initializeOrLoadInterstitialAd(applicationContext)
        mViewModel = ViewModelProvider(this).get(ViewModel::class.java)
        if (savedInstanceState == null) {
            mViewModel!!.initialize()
            setLocalizedConst()
            if (mViewModel!!.isFirstRun) {
                addFragment(AskForLook.newInstance())
            } else {
                setDayFragment()
            }
        } else {
            if (mViewModel!!.isRecreatedOnDemand) {
                setDayFragment()
            }
        }
        //addCrashTestButton();
    }

    //    private void addCrashTestButton() {
    //        Button crashButton = new Button(this);
    //        crashButton.setText("Crash!");
    //        crashButton.setOnClickListener(new View.OnClickListener() {
    //            public void onClick(View view) {
    //                throw new RuntimeException("Test Crash"); // Force a crash
    //            }
    //        });
    //
    //        addContentView(crashButton, new ViewGroup.LayoutParams(
    //                ViewGroup.LayoutParams.MATCH_PARENT,
    //                ViewGroup.LayoutParams.WRAP_CONTENT));
    //    }
    private fun setLocalizedConst() {
        if (!LocalizedConst.isSet()) {
            LocalizedConst.setWholeDay(getString(R.string.whole_day))
            LocalizedConst.setSince(getString(R.string.since))
            LocalizedConst.setAskForLook(getString(R.string.ask_for_look))
            LocalizedConst.setSet()
        }
    }

    private fun setDayFragment() {
        showWidgetInfoIfNeeded()
        addFragment(DaysFragment.newInstance())
        mViewModel!!.logLook()
    }

    private fun showWidgetInfoIfNeeded() {
        if (!mViewModel!!.wasAppWidgetInfoDisplayed()) {
            WidgetInfoDialog
                    .newInstance()
                    .show(supportFragmentManager, "")
        }
    }

    override fun setDefaultLocale(context: Context): Context {
        LocalizedConst.setLocale(Locale.getDefault())
        return super.setDefaultLocale(context)
    }

    //region Ads Handling region
    private fun setupBannerAds() {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(applicationContext)
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    private fun initializeOrLoadInterstitialAd(context: Context) {
        if (interstitialAd == null) {
            interstitialAd = InterstitialAd(context)
            interstitialAd!!.adUnitId = context.getString(R.string.interstitial_ad_id)
        }
        loadInterstitialAd()
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

    private fun showBigAd() {
        val interstitialAd = getInterstitialAd()
        interstitialAd?.show()
    }

    //endregion
    override fun onResume() {
        super.onResume()
        if (mViewModel!!.checkIfDayTypesChanged()) {
            refreshList()
        }
        if (mViewModel!!.isLookRich) {
            setLookRich()
        }
        //        showStartTime();
    }

    private fun refreshList() {
        //addFragment(new DaysFragment());
        val fragment = supportFragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG)
        if (fragment is DaysFragment) {
            fragment.setRecyclerViewAdapter()
        }
    }

    private fun setLookRich() {
        val recyclerViewContainer = findViewById<View>(R.id.main_view)
        recyclerViewContainer.setBackgroundResource(R.drawable.bg_red)
        val adViewLayout = findViewById<View>(R.id.adViewLayout)
        adViewLayout.setBackgroundResource(R.drawable.bg_red)
    }

    private fun addFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        val oldFragment = supportFragmentManager.findFragmentByTag(MAIN_FRAGMENT_TAG)
        if (oldFragment != null) ft.detach(oldFragment)
        ft.add(R.id.main_fragment, fragment, MAIN_FRAGMENT_TAG)
        ft.commit()
        supportFragmentManager.executePendingTransactions()
    }

    //    private void showStartTime() {
    //        Calendar calEnd = Calendar.getInstance();
    //        String timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
    //        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();
    //    }
    //endregion
    //region AppMenuHandlingRegion
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuLayoutId = if (mViewModel!!.isFirstRun) R.menu.main_activity_actions_first_run else R.menu.main_activity_actions
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
                mViewModel!!.isActionAddList = true
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
        val intent = Intent(applicationContext, SActivity::class.java)
        intent.putExtra(
                ViewModel.START_FRAGMENT_ID_TAG,
                ViewModel.getStartFragmentId(startFromNotifications))
        startActivity(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkCalendarPermissions() {
        if (mViewModel!!.areCalendarPermissionsGranted()) {
            checkCalendarSettings()
        } else {
            requestCalendarPermissions()
        }
    }

    private fun checkCalendarSettings() {
        if (mViewModel!!.isCalendarEnabledAndChosen) {
            if (mViewModel!!.isActionAddList) {
                askForDayTypesAddedToCalendar()
            } else {
                askForReminderSettings()
            }
        } else {
            startSettingsActivity(true)
        }
    }

    private fun askForReminderSettings() {
        mViewModel!!.setReminderDialogArgs()
        ReminderSettingsDialog
                .newInstance()
                .show(supportFragmentManager, "")
    }

    private fun askForDayTypesAddedToCalendar() {
        DayTypesAddedToCalendarDialog
                .newInstance()
                .show(supportFragmentManager, "dialog")
    }

    private fun startHelpActivity() {
        val intent = Intent(applicationContext, HActivity::class.java)
        startActivity(intent)
    }

    //endregion
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onItemClick(day: Day) {
        mViewModel!!.setLastTouchedDay(day)
        checkCalendarPermissions()
    }

    //region ShowingInfoRegion
    private fun showInfo(resultIsPositive: Boolean, positiveInfo: Int, negativeInfo: Int) {
        val info = if (resultIsPositive) positiveInfo else negativeInfo
        showInfo(info, false)
    }

    private fun showInfo(info: Int, isLengthLong: Boolean) {
        val length = if (isLengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast
                .makeText(applicationContext, info, length)
                .show()
    }

    //endregion
    override fun onCalendarPermissionsGranted() {
        checkCalendarSettings()
    }

    //region on...ClickRegion
    override fun onDialogPositiveClick(dialog: NoticeDialogFragment) {
        if (dialog is DayTypesAddedToCalendarDialog) {
            askForReminderSettings()
        } else if (dialog is ReminderSettingsDialog) {
            addToCalendar()
        }
    }

    private fun addToCalendar() {
        showBigAd()
        val isResultPositive = mViewModel!!.addToCalendar()
        if (isResultPositive) {
            refreshList()
        }
        showInfo(isResultPositive, R.string.info_added_to_calendar, mViewModel!!.error)
    }

    override fun onDialogNegativeClick(dialog: NoticeDialogFragment) {
        if (dialog is DayTypesAddedToCalendarDialog || dialog is ReminderSettingsDialog) {
            showInfo(R.string.info_cancelled, false)
        }
    }

    override fun onViewClick(noticeDialogFragment: NoticeDialogFragment) {
        noticeDialogFragment.show(supportFragmentManager, "")
    }

    fun onLookButtonClick(view: View) {
        val isLookRich = view.id == R.id.imageButtonRich
        mViewModel!!.onInitialLookChoice(isLookRich)
        removeAllFragments()
        recreate()
    } //endregion

    companion object {
        private const val MAIN_FRAGMENT_TAG = "main_fragment"
    }
}