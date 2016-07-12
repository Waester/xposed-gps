package com.github.fpi;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class AppChooser extends Activity {

    private Settings settings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_chooser);

        settings = new Settings(getApplicationContext());

        installedApps();
        refreshAppList();
    }

    private void installedApps() {
        List<PackageInfo> Apps = getPackageManager().getInstalledPackages(0);

        // Sort list
        Collections.sort(Apps, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo p1, PackageInfo p2) {
                return String.CASE_INSENSITIVE_ORDER.compare(p1.packageName, p2.packageName);
            }
        });

        TextView tv = (TextView) findViewById(R.id.installedApps);
        tv.setText("");

        for (PackageInfo appName : Apps) {
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
        List<String> Apps = new ArrayList<String>(settings.getApps());
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
            HashSet<String> Apps = settings.getApps();
            if (!Apps.contains(appName)) {
                Apps.add(appName);
            } else {
                Apps.remove(appName);
            }
            settings.updateApps(Apps);

            refreshAppList();
        }
    }
}
