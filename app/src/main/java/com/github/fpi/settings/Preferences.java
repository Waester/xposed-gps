package com.github.fpi.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;

public class Preferences {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefEditor;

    public double LATITUDE = Constants.LATITUDE;
    public double LONGITUDE = Constants.LONGITUDE;
    public float BEARING = Constants.BEARING;
    public float SPEED = Constants.SPEED;
    public float ZOOM = Constants.ZOOM;
    public boolean START = Constants.START;
    public HashSet<String> APPS = new HashSet<String>();

    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_WORLD_READABLE);
        prefEditor = sharedPreferences.edit();
    }

    public void update() {
        prefEditor.putLong("latitude", Double.doubleToRawLongBits(LATITUDE));
        prefEditor.putLong("longitude", Double.doubleToRawLongBits(LONGITUDE));
        prefEditor.putFloat("bearing", BEARING);
        prefEditor.putFloat("speed", SPEED);
        prefEditor.putFloat("zoom", ZOOM);
        prefEditor.putBoolean("start", START);
        prefEditor.apply();
    }

    public void updateApps(HashSet<String> Apps) {
        // Workaround Set bug
        prefEditor.remove("apps");
        prefEditor.apply();
        prefEditor.putStringSet("apps", Apps);
        prefEditor.apply();
    }

    public void load() {
        LATITUDE = Double.longBitsToDouble(sharedPreferences.getLong("latitude", Double.doubleToRawLongBits(LATITUDE)));
        LONGITUDE = Double.longBitsToDouble(sharedPreferences.getLong("longitude", Double.doubleToRawLongBits(LONGITUDE)));
        ZOOM = sharedPreferences.getFloat("zoom", ZOOM);
        START = sharedPreferences.getBoolean("start", START);
        APPS = (HashSet<String>) sharedPreferences.getStringSet("apps", APPS);
        //Log.d(Constants.TAG, "SharedPreferences " + sharedPreferences.getAll().toString());
    }
}
