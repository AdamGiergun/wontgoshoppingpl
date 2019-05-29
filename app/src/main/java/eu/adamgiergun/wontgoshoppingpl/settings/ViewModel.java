package eu.adamgiergun.wontgoshoppingpl.settings;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel;

import static eu.adamgiergun.wontgoshoppingpl.settings.SettingsFragment.HEADERS;
import static eu.adamgiergun.wontgoshoppingpl.settings.SettingsFragment.LOOK;
import static eu.adamgiergun.wontgoshoppingpl.settings.SettingsFragment.NOTIFICATIONS;

public class ViewModel extends AppViewModel {
    private final String SETTINGS_TITLE;
    private final String LOOK_TITLE;
    private final String NOTIFICATIONS_TITLE;

    private boolean lastIsScreenWideEnough;
    private CharSequence title;
    private String lastFragmentId;
    private StartedPreference startedPreference;

    public ViewModel(@NonNull Application application) {
        super(application);
        lastIsScreenWideEnough = isScreenWideEnough();
        SETTINGS_TITLE = getAppContext().getString(R.string.title_activity_settings);
        LOOK_TITLE = getAppContext().getString(R.string.pref_header_look);
        NOTIFICATIONS_TITLE = getAppContext().getString(R.string.pref_header_notifications);
    }

    boolean isScreenWideEnough() {
        return isScreenSizeLarge() || isOrientationLandscape();
    }

    private boolean isScreenSizeLarge() {
        int screenSize = getAppContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return (screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE);
    }
    
    private boolean isOrientationLandscape() {
        int orientation = getAppContext().getResources().getConfiguration().orientation;
        return (orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    boolean hasScreenWideEnoughChanged() {
        boolean result = (lastIsScreenWideEnough != isScreenWideEnough());
        lastIsScreenWideEnough = isScreenWideEnough();
        return result;
    }

    SettingsFragment.List getInitialFragments(String startFragmentId) {
        if (startFragmentId == null) {
            startFragmentId = isScreenWideEnough() ? LOOK : HEADERS;
        }

        SettingsFragment.List fragments = new SettingsFragment.List();
        fragments.add(SettingsFragment.newInstance(
                HEADERS,
                isScreenWideEnough() ? R.id.headers : R.id.settings,
                false));

        if (isScreenWideEnough() || !startFragmentId.equals(HEADERS)) {
            lastFragmentId = (startFragmentId.equals(LOOK) || startFragmentId.equals(HEADERS)) ?
                    LOOK :
                    startFragmentId;
            fragments.add(SettingsFragment.newInstance(
                    lastFragmentId,
                    R.id.settings,
                    !isScreenWideEnough()));
        } else {
            lastFragmentId = startFragmentId;
        }
        setTitle();
        return fragments;
    }

    private void setTitle() {
        if (lastFragmentId.equals(LOOK))
            title = LOOK_TITLE;
        else if (lastFragmentId.equals(NOTIFICATIONS))
            title = NOTIFICATIONS_TITLE;
        else
            title = SETTINGS_TITLE;
    }

    SettingsFragment.List getRotatedFragments() {
        return getInitialFragments(lastFragmentId);
    }

    void setStartedPreference(StartedPreference startedPreference) {
        this.startedPreference = startedPreference;
    }

    void setLastFragmentId(String fragmentId) {
        lastFragmentId = fragmentId;
        setTitle();
    }

    void setLastFragmentIdHeaders() {
        setLastFragmentId(HEADERS);
    }

    StartedPreference getStartedPreference() {
        return startedPreference;
    }

    boolean isStartedPreferenceNotNotifications() {
        return !startedPreference.pref.getFragment().equals(NOTIFICATIONS);
    }

    CharSequence geTitle() {
        return title;
    }
}
