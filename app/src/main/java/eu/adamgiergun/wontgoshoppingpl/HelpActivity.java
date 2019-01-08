package eu.adamgiergun.wontgoshoppingpl;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivityWithTheme {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setupHomeInBar();
        setTexts();
    }

    private void setTexts() {
        int[] textViewIds = new int[] {
                R.id.helpTextView1,
                R.id.helpTextView2,
                R.id.helpTextView3,
                R.id.helpTextView4,
                R.id.helpTextView5
        };
        TextView textView;
        int i = 0;
        for (int textViewId : textViewIds) {
            textView = findViewById(textViewId);
            textView.setText(LocalizedConst.getHelpText(i++));
        }
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
