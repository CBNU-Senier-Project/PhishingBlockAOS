package com.example.phishingblock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
            // 통화 시작
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(1000);  // 진동
            }
            // 알림 표시 로직
            NotificationUtils.showRecordingNotification(context);
            Log.d("PhoneStateReceiver", "통화 시작: 알림을 띄움");
        }
    }
}
