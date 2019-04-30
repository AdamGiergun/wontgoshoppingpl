package eu.adamgiergun.wontgoshoppingpl.settings;

import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
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
            headersTag = SettingsViewModel.HEADERS_LAND;
            lookTag = SettingsViewModel.LOOK_LAND;
            notificationsTag = SettingsViewModel.NOTIFICATIONS_LAND;
            switch (lastFragment) {
                case SettingsViewModel.HEADERS:
                    lastFragment = SettingsViewModel.HEADERS_LAND;
                    break;
                case SettingsViewModel.LOOK:
                    lastFragment = SettingsViewModel.LOOK_LAND;
                    break;
                case SettingsViewModel.NOTIFICATIONS:
                    lastFragment = SettingsViewModel.NOTIFICATIONS_LAND;
                    break;
                default:
                    break;
            }
        } else {
            headersTag = SettingsViewModel.HEADERS;
            lookTag = SettingsViewModel.LOOK;
            notificationsTag = SettingsViewModel.NOTIFICATIONS;
            switch (lastFragment) {
                case SettingsViewModel.HEADERS_LAND:
                    lastFragment = SettingsViewModel.HEADERS;
                    break;
                case SettingsViewModel.LOOK_LAND:
                    lastFragment = SettingsViewModel.LOOK;
                    break;
                case SettingsViewModel.NOTIFICATIONS_LAND:
                    lastFragment = SettingsViewModel.NOTIFICATIONS;
                    break;
                default:
                    break;
            }
        }
    }

    boolean shouldBeDetached(boolean screenNotWideEnough, String fragmentTag) {
        if (screenNotWideEnough) {
            return !getLastFragment().equals(fragmentTag);
        } else {
            if (fragmentTag.equals(getHeadersTag()) ||
                    (getLastFragment().equals(getHeadersTag()) && fragmentTag.equals(getLookTag()))) {
                return false;
            } else {
                return !getLastFragment().equals(fragmentTag);
            }
        }
    }
}
