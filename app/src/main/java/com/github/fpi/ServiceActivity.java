package com.github.fpi;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

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

    private void updateLocation() {
        Random rand = new Random();

        double earth = 6378137;
        double min = 1 / earth * -1;
        double max = 1 / earth * 2;
        double dLat = min + rand.nextDouble() * max;
        double dLng = min + rand.nextDouble() * max;
        newLat = settings.getLat() + Math.toDegrees(dLat);
        newLng = settings.getLng() + Math.toDegrees(dLng / Math.cos(Math.toRadians(settings.getLat())));
    }

    private Bundle modifyBundle(Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey("wifiScan")) {
                bundle.remove("wifiScan");
            }
        }
        return bundle;
    }

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) {
        settings.reload();
        HashSet<String> appsToHook = settings.getApps();

        if (appsToHook.contains(loadPackageParam.packageName)) {
            HashSet<String> Classes = new HashSet<String>();
            Classes.add("android.location.Location");
            Classes.add("android.net.wifi.WifiManager");
            Classes.add("android.telephony.TelephonyManager");

            HashSet<String> methodsToHook = new HashSet<String>();
            for (Methods method : Methods.values()) {
                methodsToHook.add(method.toString());
            }

            //Log.d(TAG, "Trying to hook " + loadPackageParam.packageName);

            XC_MethodHook methodHook = new XMethodHook();

            for (String clazz : Classes) {
                try {
                    Class<?> hookClass = findClass(clazz, loadPackageParam.classLoader);
                    for (Method method : hookClass.getDeclaredMethods()) {
                        if (methodsToHook.contains(method.getName())) {
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

    private enum Methods {
        getLatitude,
        getLongitude,
        getBearing,
        getSpeed,
        getExtras,
        getScanResults,
        getAllCellInfo,
        getCellLocation,
        getNeighboringCellInfo
    }

    private class XMethodHook extends XC_MethodHook {

        @Override
        protected void afterHookedMethod(MethodHookParam param) {
            settings.reload();

            /* Injection of the faked gps data */
            if (settings.isStarted()) {
                updateLocation();

                switch (Methods.valueOf(param.method.getName())) {
                    case getLatitude:
                        param.setResult(newLat);
                        //Log.v(TAG, "getLatitude " + param.getResult());
                        break;

                    case getLongitude:
                        param.setResult(newLng);
                        //Log.v(TAG, "getLongitude " + param.getResult());
                        break;

                    case getBearing:
                        param.setResult(settings.getBearing());
                        //Log.v(TAG, "getBearing " + param.getResult());
                        break;

                    case getSpeed:
                        param.setResult(settings.getSpeed());
                        //Log.v(TAG, "getSpeed " + param.getResult());
                        break;

                    case getExtras:
                        Bundle bundle = (Bundle) param.getResult();
                        param.setResult(modifyBundle(bundle));
                        //Log.v(TAG, "getExtras " + param.getResult());
                        break;

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
