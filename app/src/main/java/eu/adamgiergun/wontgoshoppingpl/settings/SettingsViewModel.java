package eu.adamgiergun.wontgoshoppingpl.settings;

import android.app.Application;

import androidx.annotation.NonNull;

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

    String getLastFragment() {
        return lastFragment;
    }

    String getHeadersTag() {
        return headersTag;
    }

    String getLookTag() {
        return lookTag;
    }

    String getNotificationsTag() {
        return notificationsTag;
    }

    boolean isStartFragmentNotification() {
        return startFragmentNotification;
    }

    void setStartFragmentNotification(boolean startFragmentNotification) {
        this.startFragmentNotification = startFragmentNotification;
    }

    int getSettingsFragmentId() {
        return settingsFragmentId;
    }

    void setSettingsFragmentId(int fragmentId) {
        settingsFragmentId = fragmentId;
    }

    void setLastFragment(String tag) {
        lastFragment = tag;
    }

    void setTagsAndFragments(boolean isOrientationLandscape) {
        if (isOrientationLandscape) {
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

    boolean shouldBeDetached(boolean screenNotWideEnough, String fragmentTag) {
        if (screenNotWideEnough) {
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
