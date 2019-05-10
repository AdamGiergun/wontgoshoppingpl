package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import eu.adamgiergun.mylibrary.AppCompatActivityWithHomeInBar;
import eu.adamgiergun.wontgoshoppingpl.R;

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

    protected Context setDefaultLocale(Context context) {
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void requestCalendarPermissions() {
        requestPermissions(AppViewModel.getCalendarPermissions(), AppViewModel.getCalendarPermissionsCode());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppViewModel.getCalendarPermissionsCode()) {
            if (AppViewModel.areAllPermissionsGranted(grantResults)) {
                onAllPermissionsGranted();
            } else {
                Toast.makeText(getApplicationContext(), eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onAllPermissionsGranted() {}
}
