package eu.adamgiergun.wontgoshoppingpl.settings;

import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;

class FragmentWithTag {
    final PreferenceFragmentCompat fragment;
    final String tag;
    final boolean detach;
    final int containerId;

    private FragmentWithTag(PreferenceFragmentCompat fragment, String tag, boolean detach, int containerId) {
        this.fragment = fragment;
        this.tag = tag;
        this.detach = detach;
        this.containerId = containerId;
    }

    static FragmentWithTag newInstance(PreferenceFragmentCompat fragment, String tag, boolean detach, int containerId) {
        return new FragmentWithTag(fragment, tag, detach, containerId);
    }

    static class List extends ArrayList<FragmentWithTag> {
    }
}
