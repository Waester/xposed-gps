package com.github.fpi.hooks;

import com.github.fpi.settings.XPreferences;

import java.lang.reflect.Method;
import java.util.HashSet;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findClass;

public class GPS implements IXposedHookLoadPackage {

    private XPreferences xPreferences = new XPreferences();

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) {
        xPreferences.load();
        HashSet<String> appsToHook = xPreferences.APPS;

        if (appsToHook.contains(loadPackageParam.packageName)) {
            HashSet<String> Classes = new HashSet<String>();
            Classes.add("android.location.Location");

            HashSet<String> methodsToHook = new HashSet<String>();
            for (Methods method : Methods.values()) {
                methodsToHook.add(method.toString());
            }

            //Log.d(Constants.TAG, "Trying to hook " + loadPackageParam.packageName);

            XC_MethodHook methodHook = new XMethodHook();

            for (String clazz : Classes) {
                try {
                    Class<?> hookClass = findClass(clazz, loadPackageParam.classLoader);
                    for (Method method : hookClass.getDeclaredMethods()) {
                        if (methodsToHook.contains(method.getName())) {
                            XposedBridge.hookMethod(method, methodHook);
                            //Log.d(Constants.TAG, "Hooking method " + method.getName());
                        }
                    }
                } catch (ClassNotFoundError ex) {
                    //Log.d(Constants.TAG, "Class " + clazz + " not found");
                }
            }
            //Log.d(Constants.TAG, loadPackageParam.packageName + " successfully hooked");
        }
    }

    private enum Methods {
        getLatitude,
        getLongitude,
        getBearing,
        getSpeed
    }

    private class XMethodHook extends XC_MethodHook {

        @Override
        protected void afterHookedMethod(MethodHookParam param) {
            xPreferences.load();

            /* Injection of the faked gps data */
            if (xPreferences.START) {

                switch (Methods.valueOf(param.method.getName())) {
                    case getLatitude:
                        param.setResult(xPreferences.LATITUDE);
                        //Log.d(Constants.TAG, "getLatitude " + param.getResult());
                        break;

                    case getLongitude:
                        param.setResult(xPreferences.LONGITUDE);
                        //Log.d(Constants.TAG, "getLongitude " + param.getResult());
                        break;

                    case getBearing:
                        param.setResult(xPreferences.BEARING);
                        //Log.d(Constants.TAG, "getBearing " + param.getResult());
                        break;

                    case getSpeed:
                        param.setResult(xPreferences.SPEED);
                        //Log.d(Constants.TAG, "getSpeed " + param.getResult());
                        break;
                }
            }
        }
    }
}
