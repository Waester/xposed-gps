package com.github.fpi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.fpi.settings.Constants;

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.TOGGLE_ACTION.equals(intent.getAction())) {
            JoystickService.toggleJoystick();
        }
    }
}
