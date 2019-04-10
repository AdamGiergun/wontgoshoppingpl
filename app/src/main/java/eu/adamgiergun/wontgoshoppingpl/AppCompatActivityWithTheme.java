package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

import eu.adamgiergun.mylibrary.AppCompatActivityWithHomeInBar;
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity;

public abstract class AppCompatActivityWithTheme extends AppCompatActivityWithHomeInBar {

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

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            super.attachBaseContext(newBase);
            setDefaultLocale(newBase);
        } else {
            super.attachBaseContext(setDefaultLocale(newBase));
        }
    }

    private Context setDefaultLocale(Context context) {
        if (this instanceof MainActivity) {
            LocalizedConst.setLocale(Locale.getDefault());
        }
        //Locale.setDefault(LocalizedConst.getLocale());
        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setLocale(config);
        } else {
            if (LocalizedConst.getLocale() != null)
                config.setLocale(LocalizedConst.getLocale());
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            update(config);
            return null;
        } else {
            return context.createConfigurationContext(config);
        }
    }

    private void setLocale(Configuration config) {
        if (LocalizedConst.getLocale() != null)
            config.locale = LocalizedConst.getLocale();
    }

    private void update(Configuration config) {
        getResources().updateConfiguration(
                config,
                getResources().getDisplayMetrics());
    }
}
