package eu.adamgiergun.wontgoshoppingpl;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;

abstract class AppCompatActivityWithTheme extends AppCompatActivity {

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        AppPreferences appPreferences = new AppPreferences(this);
        if (appPreferences.isLookRich(this)) {
            theme.applyStyle(R.style.AppThemeCustomizationsRich, true);
        } else {
            theme.applyStyle(R.style.AppThemeCustomizationsMini, true);
        }
        return theme;
    }

    void setupHomeInBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
}
