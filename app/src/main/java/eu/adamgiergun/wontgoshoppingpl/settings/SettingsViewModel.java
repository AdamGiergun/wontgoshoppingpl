package eu.adamgiergun.wontgoshoppingpl.settings;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel;

public class SettingsViewModel extends AppViewModel {
    static final String HEADERS = "headers";
    private static final String HEADERS_LAND = "headersWide";
    private static final String LOOK = "look";
    private static final String NOTIFICATIONS = "notifications";
    private static final String LOOK_LAND = "lookWide";
    private static final String NOTIFICATIONS_LAND = "notificationsWide";

    private String headersTag;
    private String lookTag;
    private String notificationsTag;
    private String lastFragment = HEADERS;
    private boolean startFragmentNotification;
    private int settingsFragmentId;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    void initialize(boolean startFromNotifications) {
        startFragmentNotification = startFromNotifications;
        setTagsAndFragments();
        setSettingsFragmentId();
    }

    private void setTagsAndFragments() {
        if (isOrientationLandscape()) {
            headersTag = HEADERS_LAND;
            lookTag = LOOK_LAND;
            notificationsTag = NOTIFICATIONS_LAND;

            switch (lastFragment) {
                case HEADERS:
                    lastFragment = HEADERS_LAND;
                    break;
                case LOOK:
                    lastFragment = LOOK_LAND;
                    break;
                case NOTIFICATIONS:
                    lastFragment = NOTIFICATIONS_LAND;
                    break;
                default:
                    break;
            }
        } else {
            headersTag = HEADERS;
            lookTag = LOOK;
            notificationsTag = NOTIFICATIONS;

            switch (lastFragment) {
                case HEADERS_LAND:
                    lastFragment = HEADERS;
                    break;
                case LOOK_LAND:
                    lastFragment = LOOK;
                    break;
                case NOTIFICATIONS_LAND:
                    lastFragment = NOTIFICATIONS;
                    break;
                default:
                    break;
            }
        }
    }

    private void setSettingsFragmentId() {
        if (isScreenSizeLesserThanLarge()) {
            settingsFragmentId = isOrientationLandscape() ?
                    R.id.settingsFragmentLand :
                    R.id.settingsFragment;
        } else {
            settingsFragmentId = isOrientationLandscape() ?
                    R.id.settingsFragmentLargeLand :
                    R.id.settingsFragmentLarge;
        }
    }

    String getHeadersTag() {
        return headersTag;
    }

    String getLastFragment() {
        return lastFragment;
    }

    String getLookTag() {
        return lookTag;
    }

    String getNotificationsTag() {
        return notificationsTag;
    }

    int getSettingsFragmentId() {
        return settingsFragmentId;
    }

    String getSwitchedFragmentTag() {
        return isScreenNotWideEnough() ? getHeadersTag() : getLookTag();
    }

    boolean isScreenSizeLesserThanLarge() {
        int screenSize = getAppContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return (screenSize < Configuration.SCREENLAYOUT_SIZE_LARGE);
    }

    boolean isScreenNotWideEnough() {
        return (!isOrientationLandscape()) && isScreenSizeLesserThanLarge();
    }

    private boolean isOrientationLandscape() {
        int orientation = getAppContext().getResources().getConfiguration().orientation;
        return (orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    boolean isStartFragmentNotification() {
        boolean result = startFragmentNotification;
        startFragmentNotification = false;
        return result;
    }

    void setLastFragment(String tag) {
        lastFragment = tag;
    }

    boolean shouldBeDetached(String fragmentTag) {
        if (isScreenNotWideEnough()) {
            return !fragmentTag.equals(lastFragment);
        } else {
            if (fragmentTag.equals(headersTag) ||
                    (fragmentTag.equals(lookTag) && lastFragment.equals(headersTag))) {
                return false;
            } else {
                return !fragmentTag.equals(lastFragment);
            }
        }
    }
}
