package com.github.fpi;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;

import de.robv.android.xposed.XSharedPreferences;

class Settings {

    private String TAG = "FPI";
    private XSharedPreferences xSharedPreferences = null;
    private SharedPreferences sharedPreferences = null;

    public Settings() {
        xSharedPreferences = new XSharedPreferences(MainActivity.class.getPackage().getName());
    }

    public Settings(Context context) {
        sharedPreferences = context.getSharedPreferences(MainActivity.class.getPackage().getName() + "_preferences", Context.MODE_WORLD_READABLE);
    }

    public double getLat() {
        if (sharedPreferences != null) {
            return Double.longBitsToDouble(sharedPreferences.getLong("latitude", Double.doubleToRawLongBits(22.2855200)));
        } else if (xSharedPreferences != null) {
            return Double.longBitsToDouble(xSharedPreferences.getLong("latitude", Double.doubleToRawLongBits(22.2855200)));
        }
        return 22.2855200;
    }

    public double getLng() {
        if (sharedPreferences != null) {
            return Double.longBitsToDouble(sharedPreferences.getLong("longitude", Double.doubleToRawLongBits(114.1576900)));
        } else if (xSharedPreferences != null) {
            return Double.longBitsToDouble(xSharedPreferences.getLong("longitude", Double.doubleToRawLongBits(114.1576900)));
        }
        return 114.1576900;
    }

    public float getZoom() {
        if (sharedPreferences != null) {
            return sharedPreferences.getFloat("zoom", 12f);
        } else if (xSharedPreferences != null) {
            return xSharedPreferences.getFloat("zoom", 12f);
        }
        return 12f;
    }

    public HashSet<String> getApps() {
        HashSet<String> defaultApps = new HashSet<String>();
        defaultApps.add("com.nianticlabs.pokemongo");
        defaultApps.add("com.nianticproject.ingress");

        if (sharedPreferences != null) {
            return (HashSet<String>) sharedPreferences.getStringSet("apps", defaultApps);
        } else if (xSharedPreferences != null) {
            return (HashSet<String>) xSharedPreferences.getStringSet("apps", defaultApps);
        }

        return defaultApps;
    }

    public boolean isStarted() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean("start", false);
        } else if (xSharedPreferences != null) {
            return xSharedPreferences.getBoolean("start", false);
        }
        return false;
    }

    public void update(double la, double ln, float zm, boolean start) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putLong("latitude", Double.doubleToRawLongBits(la));
        prefEditor.putLong("longitude", Double.doubleToRawLongBits(ln));
        prefEditor.putFloat("zoom", zm);
        prefEditor.putBoolean("start", start);
        prefEditor.apply();
    }

    public void updateApps(HashSet<String> Apps) {
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        // Workaround Set bug
        prefEditor.remove("apps");
        prefEditor.apply();
        prefEditor.putStringSet("apps", Apps);
        prefEditor.apply();
    }

    public void reload() {
        xSharedPreferences.reload();
        //Log.d(TAG, "XSharedPreferences " + xSharedPreferences.getAll().toString());
    }
}
