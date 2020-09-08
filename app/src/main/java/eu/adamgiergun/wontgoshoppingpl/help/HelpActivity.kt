package eu.adamgiergun.wontgoshoppingpl.help

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import android.widget.TextView
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithTheme

class HelpActivity : ActivityWithTheme() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        setupHomeInBar()
        activateLinks()
    }

    private fun activateLinks() {
        activateLink(R.id.privacyPolicyLink)
        activateLink(R.id.helpTextView3)
    }

    private fun activateLink(viewId: Int) {
        val view = findViewById<TextView>(viewId)
        view.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}