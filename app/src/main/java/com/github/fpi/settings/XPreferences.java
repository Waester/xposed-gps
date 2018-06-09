package com.github.fpi.settings;

import com.github.fpi.MainActivity;

import java.util.HashSet;

import de.robv.android.xposed.XSharedPreferences;

public class XPreferences {

    private String TAG = "FPI";
    private XSharedPreferences xSharedPreferences;

    public double LATITUDE = 22.2855200;
    public double LONGITUDE = 114.1576900;
    public float BEARING = 0.0f;
    public float SPEED = 0.0f;
    public boolean START = false;
    public HashSet<String> APPS = new HashSet<String>();

    public XPreferences() {
        xSharedPreferences = new XSharedPreferences(MainActivity.class.getPackage().getName());
    }

    public void load() {
        xSharedPreferences.reload();

        LATITUDE = Double.longBitsToDouble(xSharedPreferences.getLong("latitude", Double.doubleToRawLongBits(LATITUDE)));
        LONGITUDE = Double.longBitsToDouble(xSharedPreferences.getLong("longitude", Double.doubleToRawLongBits(LONGITUDE)));
        BEARING = xSharedPreferences.getFloat("bearing", BEARING);
        SPEED = xSharedPreferences.getFloat("speed", SPEED);
        START = xSharedPreferences.getBoolean("start", START);
        APPS = (HashSet<String>) xSharedPreferences.getStringSet("apps", APPS);
        //Log.d(TAG, "XSharedPreferences " + xSharedPreferences.getAll().toString());
    }
}
