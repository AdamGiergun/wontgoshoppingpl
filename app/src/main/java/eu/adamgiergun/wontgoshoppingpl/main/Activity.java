package eu.adamgiergun.wontgoshoppingpl.main;
/*
    TODO "remove all events from calendar" in Settings
    TODO "Info" group in Settings(?)
    TODO move events filtering from settings to app bar(?)
*/
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.*;
import androidx.lifecycle.*;

import com.google.android.gms.ads.*;

import java.util.Locale;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;

import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithTheme;
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst;
import eu.adamgiergun.wontgoshoppingpl.day.Day;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.help.HActivity;
import eu.adamgiergun.wontgoshoppingpl.settings.SActivity;

public class Activity extends ActivityWithTheme implements
        NoticeDialogFragment.NoticeDialogListener,
        DaysFragment.onItemClickListener,
        NoticeDialogFragment.NoticeDialogViewListener {

    private static final String MAIN_FRAGMENT_TAG = "main_fragment";
    private InterstitialAd interstitialAd;

    private ViewModel mViewModel;
//    private Calendar calStart;

    //region ActivityLifeCycleRegion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        calStart = Calendar.getInstance();
        setContentView(R.layout.activity_main);
        setupBannerAds();
        initializeOrLoadInterstitialAd(getApplicationContext());
        mViewModel = ViewModelProviders.of(this).get(ViewModel.class);

        if (savedInstanceState == null) {
            mViewModel.initialize();
            setLocalizedConst();
            if (mViewModel.isFirstRun()) {
                addFragment(AskForLook.newInstance());
            } else {
                setDayFragment();
            }
        } else {
            if (mViewModel.isRecreatedOnDemand()) {
                setDayFragment();
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

    private void setLocalizedConst() {
        if (!LocalizedConst.isSet()) {
            LocalizedConst.setWholeDay(getString(R.string.whole_day));
            LocalizedConst.setSince(getString(R.string.since));
            LocalizedConst.setAskForLook(getString(R.string.ask_for_look));
            LocalizedConst.setSet();
        }
    }

    private void setDayFragment() {
        showWidgetInfoIfNeeded();
        addFragment(DaysFragment.newInstance());
        mViewModel.logLook();
    }

    private void showWidgetInfoIfNeeded() {
        if (!mViewModel.wasAppWidgetInfoDisplayed()) {
            WidgetInfoDialog
                    .newInstance()
                    .show(getSupportFragmentManager(), "");
        }
    }

    @Override
    protected Context setDefaultLocale(Context context) {
        LocalizedConst.setLocale(Locale.getDefault());
        return super.setDefaultLocale(context);
    }

    //region Ads Handling region
    private void setupBannerAds() {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(getApplicationContext(), getString(R.string.ad_mob_app_id));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void initializeOrLoadInterstitialAd(Context context) {
        if (interstitialAd == null) {
            interstitialAd = new InterstitialAd(context);
            interstitialAd.setAdUnitId(context.getString(R.string.interstitial_ad_id));
        }
        loadInterstitialAd();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    private InterstitialAd getInterstitialAd() {
        if (interstitialAd == null) {
            initializeOrLoadInterstitialAd(getApplicationContext());
            return null;
        } else {
            if (interstitialAd.isLoaded()) {
                return interstitialAd;
            } else {
                if (!interstitialAd.isLoading()) {
                    loadInterstitialAd();
                }
                return null;
            }
        }
    }

    private void showBigAd() {
        InterstitialAd interstitialAd = getInterstitialAd();
        if (interstitialAd != null) {
            interstitialAd.show();
        }
    }
    //endregion

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewModel.checkIfDayTypesChanged()) {
            refreshList();
        }
        if (mViewModel.isLookRich()) {
            setLookRich();
        }
//        showStartTime();
    }

    private void refreshList() {
        //addFragment(new DaysFragment());
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (fragment instanceof DaysFragment) {
            ((DaysFragment) fragment).setRecyclerViewAdapter();
        }
    }

    private void setLookRich() {
        View recyclerViewContainer = findViewById(R.id.main_view);
        recyclerViewContainer.setBackgroundResource(R.drawable.bg_red);

        View adViewLayout = findViewById(R.id.adViewLayout);
        adViewLayout.setBackgroundResource(R.drawable.bg_red);
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (oldFragment != null)
            ft.detach(oldFragment);
        ft.add(R.id.main_fragment, fragment, MAIN_FRAGMENT_TAG);
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

//    private void showStartTime() {
//        Calendar calEnd = Calendar.getInstance();
//        String timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
//        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();
//    }
    //endregion

    //region AppMenuHandlingRegion
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuLayoutId = mViewModel.isFirstRun() ? R.menu.main_activity_actions_first_run : R.menu.main_activity_actions;
        getMenuInflater().inflate(menuLayoutId, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startSettingsActivity(false);
                return true;
            case R.id.calendar:
                mViewModel.setActionAddList(true);
                checkCalendarPermissions();
                return true;
            case R.id.help:
                startHelpActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSettingsActivity(boolean startFromNotifications) {
        Intent intent = new Intent(getApplicationContext(), SActivity.class);
        intent.putExtra(
                ViewModel.START_FRAGMENT_ID_TAG,
                ViewModel.getStartFragmentId(startFromNotifications));
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkCalendarPermissions() {
        if (mViewModel.areCalendarPermissionsGranted()) {
            checkCalendarSettings();
        } else {
            requestCalendarPermissions();
        }
    }

    private void checkCalendarSettings() {
        if (mViewModel.isCalendarEnabledAndChosen()) {
            if (mViewModel.isActionAddList()) {
                askForDayTypesAddedToCalendar();
            } else {
                askForReminderSettings();
            }
        } else {
            startSettingsActivity(true);
        }
    }

    private void askForReminderSettings() {
        mViewModel.setReminderDialogArgs();
        ReminderSettingsDialog
                .newInstance()
                .show(getSupportFragmentManager(), "");
    }

    private void askForDayTypesAddedToCalendar() {
        DayTypesAddedToCalendarDialog
                .newInstance()
                .show(getSupportFragmentManager(), "dialog");
    }

    private void startHelpActivity() {
        Intent intent = new Intent(getApplicationContext(), HActivity.class);
        startActivity(intent);
    }
    //endregion

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onItemClick(Day day) {
        mViewModel.setLastTouchedDay(day);
        checkCalendarPermissions();
    }

    //region ShowingInfoRegion
    private void showInfo(boolean resultIsPositive, int positiveInfo, int negativeInfo) {
        int info = resultIsPositive ? positiveInfo : negativeInfo;
        showInfo(info, false);
    }

    private void showInfo(int info, boolean isLengthLong) {
        int length = isLengthLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast
                .makeText(getApplicationContext(), info, length)
                .show();
    }
    //endregion

    @Override
    protected void onCalendarPermissionsGranted() {
        checkCalendarSettings();
    }

    //region on...ClickRegion
    @Override
    public void onDialogPositiveClick(NoticeDialogFragment dialog) {
        if (dialog instanceof DayTypesAddedToCalendarDialog) {
            askForReminderSettings();
        } else if (dialog instanceof ReminderSettingsDialog) {
            addToCalendar();
        }
    }

    private void addToCalendar() {
        showBigAd();
        boolean isResultPositive = mViewModel.addToCalendar();
        if (isResultPositive) {
            refreshList();
        }
        showInfo(isResultPositive, R.string.info_added_to_calendar, mViewModel.getError());
    }

    @Override
    public void onDialogNegativeClick(NoticeDialogFragment dialog) {
        if (dialog instanceof DayTypesAddedToCalendarDialog || dialog instanceof ReminderSettingsDialog) {
            showInfo(R.string.info_cancelled, false);
        }
    }

    @Override
    public void onViewClick(NoticeDialogFragment noticeDialogFragment) {
        noticeDialogFragment.show(getSupportFragmentManager(), "");
    }

    public void onLookButtonClick(View view) {
        boolean isLookRich = (view.getId() == R.id.imageButtonRich);
        mViewModel.onInitialLookChoice(isLookRich);
        removeAllFragments();
        recreate();
    }
    //endregion
}
