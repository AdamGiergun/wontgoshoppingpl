package eu.adamgiergun.wontgoshoppingpl.help

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithThemeAndPermissions
import eu.adamgiergun.wontgoshoppingpl.databinding.ActivityHelpBinding

internal class HelpActivity : ActivityWithThemeAndPermissions() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LinkMovementMethod.getInstance().let { linkMovementMethod ->
            binding.privacyPolicyLink.movementMethod = linkMovementMethod
            binding.helpTextView3.movementMethod = linkMovementMethod
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
