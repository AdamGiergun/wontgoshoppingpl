package eu.adamgiergun.wontgoshoppingpl.settings;

import androidx.lifecycle.ViewModel;

class SettingsViewModel extends ViewModel {
    static final String HEADERS = "headers";
    private static final String HEADERS_LAND = "headersWide";
    private static final String LOOK = "look";
    private static final String NOTIFICATIONS = "notifications";
    private static final String LOOK_LAND = "lookWide";
    private static final String NOTIFICATIONS_LAND = "notificationsWide";
    static String headers;
    static String look;
    static String notifications;

    private String lastFragment = HEADERS;

    String getLastFragment() {
        return lastFragment;
    }

    void setLastFragment(String tag) {
        lastFragment = tag;
    }

    void setOrientationLandscape(boolean isOrientationLandscape) {
        if (isOrientationLandscape) {
            SettingsViewModel.headers = SettingsViewModel.HEADERS_LAND;
            SettingsViewModel.look = SettingsViewModel.LOOK_LAND;
            SettingsViewModel.notifications = SettingsViewModel.NOTIFICATIONS_LAND;
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
            SettingsViewModel.headers = SettingsViewModel.HEADERS;
            SettingsViewModel.look = SettingsViewModel.LOOK;
            SettingsViewModel.notifications = SettingsViewModel.NOTIFICATIONS;
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
}
