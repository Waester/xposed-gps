package com.github.fpi;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.fpi.settings.Preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppChooser extends Activity {

    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_chooser);

        preferences = new Preferences(getApplicationContext());
        preferences.load();

        installedApps();
        refreshAppList();
    }

    private void installedApps() {
        List<PackageInfo> installedPackages = new ArrayList<PackageInfo>();

        for (PackageInfo packageInfo : getPackageManager().getInstalledPackages(0)) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                installedPackages.add(packageInfo);
            }
        }

        // Sort list
        Collections.sort(installedPackages, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo p1, PackageInfo p2) {
                return String.CASE_INSENSITIVE_ORDER.compare(p1.packageName, p2.packageName);
            }
        });

        TextView tv = (TextView) findViewById(R.id.installedApps);
        tv.setText("");

        for (PackageInfo appName : installedPackages) {
            tv.append(appName.packageName + "\n");
        }

        if (tv.getMovementMethod() == null) {
            tv.setMovementMethod(new ScrollingMovementMethod());
        }

        if (!tv.isTextSelectable()) {
            tv.setTextIsSelectable(true);
        }
    }

    private void refreshAppList() {
        List<String> Apps = new ArrayList<String>(preferences.APPS);
        Collections.sort(Apps);

        TextView tv = (TextView) findViewById(R.id.hookedApps);
        tv.setText("");

        for (String appName : Apps) {
            tv.append(appName + "\n");
        }

        if (tv.getMovementMethod() == null) {
            tv.setMovementMethod(new ScrollingMovementMethod());
        }

        if (!tv.isTextSelectable()) {
            tv.setTextIsSelectable(true);
        }
    }

    public void updateAppList(View view) {
        EditText editAppName = (EditText) findViewById(R.id.AppName);
        String appName = editAppName.getText().toString();

        if (!TextUtils.isEmpty(appName)) {
            if (!preferences.APPS.contains(appName)) {
                preferences.APPS.add(appName);
            } else {
                preferences.APPS.remove(appName);
            }
            preferences.updateApps(preferences.APPS);

            refreshAppList();
        }
    }
}
