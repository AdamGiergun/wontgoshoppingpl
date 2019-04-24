package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import androidx.lifecycle.ViewModelProviders;

import java.util.Locale;

import eu.adamgiergun.mylibrary.AppCompatActivityWithHomeInBar;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity;

public abstract class AppCompatActivityWithTheme extends AppCompatActivityWithHomeInBar {
    private AppViewModel mViewModel;

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        if (mViewModel == null) {
            mViewModel = ViewModelProviders.of(this).get(AppViewModel.class);

            int style = mViewModel.isLookRich() ? R.style.AppThemeCustomizationsRich : R.style.AppThemeCustomizationsMini;
            theme.applyStyle(style, true);
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
        Configuration config = new Configuration();

        if (LocalizedConst.getLocale() != null)
            setLocale(config);

        return updateConfiguration(context, config);
    }

    private void setLocale(Configuration config) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.locale = LocalizedConst.getLocale();
        } else {
            config.setLocale(LocalizedConst.getLocale());
        }
    }

    private Context updateConfiguration(Context context, Configuration config) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            //noinspection deprecation
            getResources().updateConfiguration(
                    config,
                    getResources().getDisplayMetrics());
            return null;
        } else {
            return context.createConfigurationContext(config);
        }
    }
}
