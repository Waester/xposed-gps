package com.github.fpi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constant.TOGGLE_ACTION.equals(intent.getAction())) {
            JoystickService.toggleJoystick();
        }
    }
}
