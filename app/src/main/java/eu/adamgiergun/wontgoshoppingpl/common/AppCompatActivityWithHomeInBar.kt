package eu.adamgiergun.wontgoshoppingpl.common

import android.content.res.Configuration
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

abstract class AppCompatActivityWithHomeInBar : AppCompatActivity() {
    protected fun setDisplayOptionsForActionBar(showTitleIfPossible: Boolean, showIcon: Boolean, iconId: Int) {
        setupHomeInBar()
        supportActionBar?.let { mActionBar ->
            showTitleIfPossible(mActionBar, showTitleIfPossible)
            if (showIcon) mActionBar.setIcon(iconId)
        }
    }

    protected fun setupHomeInBar() {
        supportActionBar?.let { mActionBar ->
            mActionBar.setDisplayHomeAsUpEnabled(true)
            mActionBar.setDisplayShowHomeEnabled(true)
        }
    }

    private fun showTitleIfPossible(mActionBar: ActionBar, alwaysShowTitle: Boolean) {
        val screenLayoutSize = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val showTitle = if (alwaysShowTitle || screenLayoutSize > Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            ActionBar.DISPLAY_SHOW_TITLE
        } else {
            0
        }
        mActionBar.displayOptions = mActionBar.displayOptions or showTitle
    }
}