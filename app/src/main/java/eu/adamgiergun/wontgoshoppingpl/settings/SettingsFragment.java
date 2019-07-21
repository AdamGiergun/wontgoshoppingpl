package eu.adamgiergun.wontgoshoppingpl.settings;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;

public class SettingsFragment {
    static final String HEADERS = HeadersPreferenceFragment.class.getName();
    static final String LOOK = LookPreferenceFragment.class.getName();
    static final String NOTIFICATIONS = NotificationPreferenceFragment.class.getName();

    final PreferenceFragmentCompat fragment;
    final int containerId;
    final boolean addToBackStack;

    private SettingsFragment(PreferenceFragmentCompat fragment, int containerId, boolean addToBackStack) {
        this.fragment = fragment;
        this.containerId = containerId;
        this.addToBackStack = addToBackStack;
    }

    static SettingsFragment newInstance(@NonNull String fragmentId, int containerId, boolean addToBackStack) {
        return new SettingsFragment(newFragment(fragmentId), containerId, addToBackStack);
    }

    private static PreferenceFragmentCompat newFragment(@NonNull String fragmentId) {
        if(fragmentId.equals(HEADERS))
            return new HeadersPreferenceFragment();
        else if (fragmentId.equals(LOOK))
            return new LookPreferenceFragment();
        else if (fragmentId.equals(NOTIFICATIONS))
            return new NotificationPreferenceFragment();
        else
            return null;
    }

    public static String getStartFragmentId(boolean startFromNotifications) {
        return startFromNotifications ? NOTIFICATIONS : HEADERS;
    }

    static class List extends ArrayList<SettingsFragment> {}
}
