package eu.adamgiergun.wontgoshoppingpl;

import android.os.Bundle;
import android.view.MenuItem;

public class HelpActivity extends AppCompatActivityWithTheme {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setupHomeInBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
