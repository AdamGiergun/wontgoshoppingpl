package eu.adamgiergun.wontgoshoppingpl;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivityWithTheme {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setupHomeInBar();
        activateLinks();
    }

    private void activateLinks() {
        activateLink(R.id.privacyPolicyLink);
        activateLink(R.id.helpTextView3);
    }

    private void activateLink(int viewId) {
        TextView view = findViewById(viewId);
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
