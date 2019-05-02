package eu.adamgiergun.wontgoshoppingpl.settings;

import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;

class FragmentWithTag {
    PreferenceFragmentCompat fragment;
    String tag;
    boolean detach;

    private FragmentWithTag() {}

    static FragmentWithTag newInstance(PreferenceFragmentCompat fragment, String tag, boolean detach) {
        FragmentWithTag fragmentWithTag = new FragmentWithTag();
        fragmentWithTag.fragment = fragment;
        fragmentWithTag.tag = tag;
        fragmentWithTag.detach = detach;
        return fragmentWithTag;
    }

    static class List extends ArrayList<FragmentWithTag> {
    }
}
