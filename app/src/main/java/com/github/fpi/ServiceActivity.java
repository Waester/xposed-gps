package com.github.fpi;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findClass;

public class ServiceActivity implements IXposedHookLoadPackage {

    private String TAG = "FPI";
    private Settings settings = new Settings();
    private double newLat;
    private double newLng;
    //private float newAcc;
    private long lastUpdate = 0;

    private void updateLocation() {
        if (SystemClock.elapsedRealtime() - lastUpdate >= 1000) {
            lastUpdate = SystemClock.elapsedRealtime();
            //Random rand = new Random();

            // http://stackoverflow.com/questions/2839533/adding-distance-to-a-gps-coordinate
            // http://stackoverflow.com/questions/15055624/generating-random-doubles-in-java
            //double earth = 6378137;
            //double min = 5 / earth * -1;
            //double max = 5 / earth * 2;
            //double dLat = min + rand.nextDouble() * max;
            //double dLng = min + rand.nextDouble() * max;
            newLat = settings.getLat();// + Math.toDegrees(dLng);
            newLng = settings.getLng();// + Math.toDegrees(dLat / Math.cos(Math.toRadians(settings.getLat())));
            // ----

            //newAcc = (float) (rand.nextInt(20) + 25);
        }
    }

    private Bundle modifyBundle(Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey("wifiScan")) {
                bundle.remove("wifiScan");
            }
            /*
            for (String key : bundle.keySet()) {
                if (bundle.get(key) instanceof Location) {
                    Location location = (Location) bundle.get(key);
                    bundle.putParcelable(key, spoofLocation(location));
                }
            }
            */
        }
        return bundle;
    }

    /*
    private Location spoofLocation(Location location) {
        if (location != null) {
            location.setLatitude(newLat);
            location.setLongitude(newLng);
            //location.setAccuracy(newAcc);

            Bundle bundle = location.getExtras();
            location.setExtras(modifyBundle(bundle));
        }
        return location;
    }
    */

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) throws Throwable {
        settings.reload();
        HashSet<String> appsToHook = settings.getApps();

        if (appsToHook.contains(loadPackageParam.packageName)) {
            HashSet<String> Classes = new HashSet<String>();
            Classes.add("android.location.Location");
            //Classes.add("android.location.LocationManager");
            Classes.add("android.net.wifi.WifiManager");
            Classes.add("android.telephony.TelephonyManager");

            HashSet<String> methodsToHook = new HashSet<String>();
            methodsToHook.add("getLatitude");
            methodsToHook.add("getLongitude");
            //methodsToHook.add("getAccuracy");
            methodsToHook.add("getExtras");
            //methodsToHook.add("getLastKnownLocation");
            //methodsToHook.add("getLastLocation");
            //methodsToHook.add("getExtraLocation");
            methodsToHook.add("getScanResults");
            methodsToHook.add("getAllCellInfo");
            methodsToHook.add("getCellLocation");
            methodsToHook.add("getNeighboringCellInfo");

            //Log.d(TAG, "Trying to hook " + loadPackageParam.packageName);

            XC_MethodHook methodHook = new XMethodHook();

            for (String clazz : Classes) {
                try {
                    Class<?> hookClass = findClass(clazz, loadPackageParam.classLoader);
                    for (Method method : hookClass.getDeclaredMethods()) {
                        int m = method.getModifiers();
                        if (Modifier.isPublic(m) && !Modifier.isStatic(m) && methodsToHook.contains(method.getName())) {
                            XposedBridge.hookMethod(method, methodHook);
                            //Log.v(TAG, "Hooking method " + method.getName());
                        }
                    }
                } catch (ClassNotFoundError ex) {
                    Log.e(TAG, "Class " + clazz + " not found");
                }
            }
            Log.i(TAG, loadPackageParam.packageName + " successfully hooked");
        }
    }

    private enum methods {
        getLatitude,
        getLongitude,
        //getAccuracy,
        getExtras,
        //getLastKnownLocation,
        //getLastLocation,
        //getExtraLocation,
        getScanResults,
        getAllCellInfo,
        getCellLocation,
        getNeighboringCellInfo
    }

    private class XMethodHook extends XC_MethodHook {

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            settings.reload();

            /* Injection of the faked gps data */
            if (settings.isStarted()) {
                updateLocation();

                switch (methods.valueOf(param.method.getName())) {

                    case getLatitude:
                        param.setResult(newLat);
                        //Log.v(TAG, "getLatitude " + param.getResult());
                        break;

                    case getLongitude:
                        param.setResult(newLng);
                        //Log.v(TAG, "getLongitude " + param.getResult());
                        break;

                    /*
                    case getAccuracy:
                        param.setResult(newAcc);
                        //Log.v(TAG, "getAccuracy " + param.getResult());
                        break;
                    */

                    case getExtras:
                        Bundle bundle = (Bundle) param.getResult();
                        param.setResult(modifyBundle(bundle));
                        //Log.v(TAG, "getExtras " + param.getResult());
                        break;

                    /*
                    case getLastKnownLocation:
                        if (param.getResult() instanceof Location) {
                            Location location = (Location) param.getResult();
                            param.setResult(spoofLocation(location));
                            //Log.v(TAG, "getLastKnownLocation " + param.getResult());
                        }
                        break;

                    case getLastLocation:
                        if (param.getResult() instanceof Location) {
                            Location location = (Location) param.getResult();
                            param.setResult(spoofLocation(location));
                            //Log.v(TAG, "getLastLocation " + param.getResult());
                        }
                        break;

                    case getExtraLocation:
                        if (param.getResult() instanceof Location) {
                            Location location = (Location) param.getResult();
                            param.setResult(spoofLocation(location));
                            //Log.v(TAG, "getExtraLocation " + param.getResult());
                        }
                        break;
                    */

                    case getScanResults:
                        if (param.getResult() != null) {
                            param.setResult(new ArrayList<ScanResult>());
                            //Log.v(TAG, "getScanResults " + param.getResult());
                        }
                        break;

                    case getAllCellInfo:
                        if (param.getResult() != null) {
                            param.setResult(new ArrayList<CellInfo>());
                            //Log.v(TAG, "getAllCellInfo " + param.getResult());
                        }
                        break;

                    case getCellLocation:
                        if (param.getResult() != null) {
                            param.setResult(CellLocation.getEmpty());
                            //Log.v(TAG, "getCellLocation " + param.getResult());
                        }
                        break;

                    case getNeighboringCellInfo:
                        if (param.getResult() != null) {
                            param.setResult(new ArrayList<NeighboringCellInfo>());
                            //Log.v(TAG, "getNeighboringCellInfo " + param.getResult());
                        }
                        break;

                }
            }
        }
    }
}
