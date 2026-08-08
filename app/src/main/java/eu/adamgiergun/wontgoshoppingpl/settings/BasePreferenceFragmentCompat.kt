//**
//* https://stackoverflow.com/questions/18509369/android-how-to-get-remove-margin-padding-in-preference-screen
//**
package eu.adamgiergun.wontgoshoppingpl.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.preference.*
import androidx.preference.R
import androidx.recyclerview.widget.RecyclerView

@Suppress("unused")
abstract class BasePreferenceFragmentCompat : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        @SuppressLint("RestrictedApi")
        val preferenceGroupAdapter =  object : PreferenceGroupAdapter(preferenceScreen) {
            @SuppressLint("RestrictedApi")
            override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val preference = getItem(position)
                if (preference is PreferenceCategory)
                    setZeroPaddingToLayoutChildren(holder.itemView)
                else
                    holder.findViewById(R.id.icon_frame).let {
                        it.visibility = if (preference?.icon == null) View.GONE else View.VISIBLE
                    }
            }
        }
        return preferenceGroupAdapter
    }

    private fun setZeroPaddingToLayoutChildren(view: View) {
        if (view is ViewGroup) {
            val childCount = view.childCount
            for (i in 0 until childCount) {
                setZeroPaddingToLayoutChildren(view.getChildAt(i))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    view.setPaddingRelative(
                            0,
                            view.paddingTop,
                            view.paddingEnd,
                            view.paddingBottom)
                else
                    view.setPadding(
                            0,
                            view.paddingTop,
                            view.paddingRight,
                            view.paddingBottom)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen
            .sharedPreferences
            ?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen
            .sharedPreferences
            ?.registerOnSharedPreferenceChangeListener(this)
    }
}