package eu.adamgiergun.wontgoshoppingpl.help

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithThemeAndPermissions
import eu.adamgiergun.wontgoshoppingpl.databinding.ActivityHelpBinding

internal class HelpActivity : ActivityWithThemeAndPermissions() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityHelpBinding = DataBindingUtil.setContentView(this, R.layout.activity_help)
        binding.linkMovementMethod = LinkMovementMethod.getInstance()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

@BindingAdapter("linkMovementMethod")
internal fun AppCompatTextView.setLinkMovementMethod(linkMovementMethod: MovementMethod) {
    movementMethod = linkMovementMethod
}