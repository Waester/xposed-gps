package com.github.fpi;

import java.lang.reflect.Method;
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

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) {
        settings.reload();
        HashSet<String> appsToHook = settings.getApps();

        if (appsToHook.contains(loadPackageParam.packageName)) {
            HashSet<String> Classes = new HashSet<String>();
            Classes.add("android.location.Location");

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
                            //Log.d(TAG, "Hooking method " + method.getName());
                        }
                    }
                } catch (ClassNotFoundError ex) {
                    //Log.d(TAG, "Class " + clazz + " not found");
                }
            }
            //Log.d(TAG, loadPackageParam.packageName + " successfully hooked");
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
            settings.reload();

            /* Injection of the faked gps data */
            if (settings.getStart()) {

                switch (Methods.valueOf(param.method.getName())) {
                    case getLatitude:
                        param.setResult(settings.getLatitude());
                        //Log.d(TAG, "getLatitude " + param.getResult());
                        break;

                    case getLongitude:
                        param.setResult(settings.getLongitude());
                        //Log.d(TAG, "getLongitude " + param.getResult());
                        break;

                    case getBearing:
                        param.setResult(settings.getBearing());
                        //Log.d(TAG, "getBearing " + param.getResult());
                        break;

                    case getSpeed:
                        param.setResult(settings.getSpeed());
                        //Log.d(TAG, "getSpeed " + param.getResult());
                        break;
                }
            }
        }
    }
}
