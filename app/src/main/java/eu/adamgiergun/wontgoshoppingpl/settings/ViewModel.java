package eu.adamgiergun.wontgoshoppingpl.settings;

import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel;

public class ViewModel extends AppViewModel {
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

    public ViewModel(@NonNull Application application) {
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
        if (isScreenSizeLarge()) {
            settingsFragmentId = isOrientationLandscape() ?
                    R.id.settingsFragmentLargeLand :
                    R.id.settingsFragmentLarge;

        } else {
            settingsFragmentId = isOrientationLandscape() ?
                    R.id.settingsFragmentLand :
                    R.id.settingsFragment;
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

    String getSwitchedToNotificationsTag() {
        return isScreenWideEnough() ? getLookTag() : getHeadersTag();
    }

    boolean isScreenSizeLarge() {
        int screenSize = getAppContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return (screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE);
    }

    boolean isScreenWideEnough() {
        return isScreenSizeLarge() || isOrientationLandscape();
    }

    boolean isScreenWideEnoughOrLastFragmentIsHEADERS() {
        return isScreenWideEnough() || lastFragment.equals(HEADERS);
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

    FragmentWithTag.Set newFragmentsWithTag() {
        FragmentWithTag.Set settingsFragments = new FragmentWithTag.Set();

        settingsFragments.add(
                FragmentWithTag.newInstance(
                        new PreferenceHeadersFragment(),
                        headersTag,
                        shouldBeDetached(headersTag),
                        isScreenWideEnough() ? R.id.headersFragment : settingsFragmentId));

        settingsFragments.add(
                FragmentWithTag.newInstance(
                        new LookPreferenceFragment(),
                        lookTag,
                        shouldBeDetached(lookTag),
                        settingsFragmentId));

        if (areCalendarPermissionsGranted()) {
            settingsFragments.add(
                    FragmentWithTag.newInstance(
                            new NotificationPreferenceFragment(),
                            notificationsTag,
                            shouldBeDetached(notificationsTag),
                            settingsFragmentId));
        }

        return settingsFragments;
    }

    FragmentWithTag.Set newNotificationPreferenceFragment() {
        FragmentWithTag.Set set = new FragmentWithTag.Set();
        set.add(
                FragmentWithTag.newInstance(
                        new NotificationPreferenceFragment(),
                        notificationsTag,
                        true,
                        settingsFragmentId));
        return set;
    }

    void setLastFragment(String tag) {
        lastFragment = tag;
    }

    private boolean shouldBeDetached(String fragmentTag) {
        if (isScreenWideEnough()) {
            if (fragmentTag.equals(headersTag) ||
                    (fragmentTag.equals(lookTag) && lastFragment.equals(headersTag))) {
                return false;
            } else {
                return !fragmentTag.equals(lastFragment);
            }
        } else {
            return !fragmentTag.equals(lastFragment);
        }
    }
}
