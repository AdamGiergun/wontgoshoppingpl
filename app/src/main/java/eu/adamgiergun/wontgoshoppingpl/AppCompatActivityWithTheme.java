package eu.adamgiergun.wontgoshoppingpl;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

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

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            super.attachBaseContext(newBase);
            setDefaultLocale(newBase);
        } else {
            super.attachBaseContext(setDefaultLocale(newBase));
        }
    }

    Context setDefaultLocale(Context context) {
        if (this instanceof MainActivity) {
            LocalizedConst.setLocale(Locale.getDefault());
        }
        //Locale.setDefault(LocalizedConst.getLocale());
        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setLocale(config);
        } else {
            config.setLocale(LocalizedConst.getLocale());
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            update(config);
            return null;
        } else {
            return context.createConfigurationContext(config);
        }
    }

    @SuppressWarnings("deprecation")
    void setLocale(Configuration config) {
        config.locale = LocalizedConst.getLocale();
    }

    @SuppressWarnings("deprecation")
    void update(Configuration config) {
        getResources().updateConfiguration(
                config,
                getResources().getDisplayMetrics());
    }

}
