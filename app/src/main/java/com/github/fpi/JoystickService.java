package com.github.fpi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class JoystickService extends Service {

    private WindowManager windowManager;
    private LayoutInflater layoutInflater;
    private View joystickView;
    private WindowManager.LayoutParams joystickViewParams;
    private Settings settings = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        settings = new Settings(getApplicationContext());

        joystickViewParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        joystickViewParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;

        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        joystickView = layoutInflater.inflate(R.layout.joystick, null);

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(joystickView, joystickViewParams);

        JoystickView joystick = (JoystickView) joystickView.findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // https://www.movable-type.co.uk/scripts/latlong.html
                double speed = ((double) strength / 100) * 4.2;
                double distance = speed / 6378137;
                double bearing = 0;
                if ((360 - angle) <= 270) {
                    bearing = Math.toRadians((360 - angle) + 90);
                } else {
                    bearing = Math.toRadians((360 - angle) - 270);
                }
                double lat1 = Math.toRadians(settings.getLat());
                double lng1 = Math.toRadians(settings.getLng());

                double lat2 = Math.toDegrees(Math.asin(Math.sin(lat1) * Math.cos(distance) + Math.cos(lat1) * Math.sin(distance) * Math.cos(bearing)));
                double lng2 = Math.toDegrees(lng1 + Math.atan2(Math.sin(bearing) * Math.sin(distance) * Math.cos(lat1), Math.cos(distance) - Math.sin(lat1) * Math.sin(lat2)));
                lng2 = (lng2 + 540) % 360 - 180;

                settings.update(lat2,lng2,(float)Math.toDegrees(bearing),(float)speed,settings.getZoom(),settings.isStarted());
            }
        }, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(joystickView);
    }
}
