package com.github.fpi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.github.fpi.settings.Constants;
import com.github.fpi.settings.Preferences;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class JoystickService extends Service {

    private WindowManager windowManager;
    private LayoutInflater layoutInflater;
    private NotificationManager notificationManager;
    private View joystickView;
    private WindowManager.LayoutParams joystickViewParams;
    private Preferences preferences;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new Preferences(getApplicationContext());

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
                preferences.load();

                // https://www.movable-type.co.uk/scripts/latlong.html
                double speed = ((double) strength / 100) * Constants.WALKING_SPEED;
                double distance = speed / Constants.EARTH_RADIUS;
                double bearing;
                if ((360 - angle) <= 270) {
                    bearing = Math.toRadians((360 - angle) + 90);
                } else {
                    bearing = Math.toRadians((360 - angle) - 270);
                }
                double lat1 = Math.toRadians(preferences.LATITUDE);
                double lng1 = Math.toRadians(preferences.LONGITUDE);

                double lat2 = Math.toDegrees(Math.asin(Math.sin(lat1) * Math.cos(distance) + Math.cos(lat1) * Math.sin(distance) * Math.cos(bearing)));
                double lng2 = Math.toDegrees(lng1 + Math.atan2(Math.sin(bearing) * Math.sin(distance) * Math.cos(lat1), Math.cos(distance) - Math.sin(lat1) * Math.sin(lat2)));
                lng2 = (lng2 + 540) % 360 - 180;

                preferences.LATITUDE = lat2;
                preferences.LONGITUDE = lng2;
                preferences.BEARING = (float) Math.toDegrees(bearing);
                preferences.SPEED = (float) speed;
                preferences.update();
            }
        }, 1000);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.TOGGLE_ACTION);

        broadcastReceiver = new ActionReceiver();
        registerReceiver(broadcastReceiver, intentFilter);

        Intent hideJoystick = new Intent();
        hideJoystick.setAction(Constants.TOGGLE_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0, hideJoystick, 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Joystick")
                .setContentText("Press to toggle joystick visibility.")
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true)
                .build();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(joystickView);
        notificationManager.cancel(Constants.NOTIFICATION_ID);
        unregisterReceiver(broadcastReceiver);
    }

    public void toggleJoystick() {
        if (joystickView.getVisibility() == View.VISIBLE) {
            joystickView.setVisibility(View.GONE);
        } else {
            joystickView.setVisibility(View.VISIBLE);
        }
    }

    public class ActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.TOGGLE_ACTION.equals(intent.getAction())) {
                toggleJoystick();
            }
        }
    }
}
