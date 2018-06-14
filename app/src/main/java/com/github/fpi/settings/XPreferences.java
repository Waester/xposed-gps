package com.github.fpi.settings;

import com.github.fpi.MainActivity;

import java.util.HashSet;

import de.robv.android.xposed.XSharedPreferences;

public class XPreferences {

    private XSharedPreferences xSharedPreferences;

    public double LATITUDE = Constants.LATITUDE;
    public double LONGITUDE = Constants.LONGITUDE;
    public float BEARING = Constants.BEARING;
    public float SPEED = Constants.SPEED;
    public boolean START = Constants.START;
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
        //Log.d(Constants.TAG, "XSharedPreferences " + xSharedPreferences.getAll().toString());
    }
}
