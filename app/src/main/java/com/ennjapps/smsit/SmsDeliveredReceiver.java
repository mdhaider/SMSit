package com.ennjapps.smsit;

/**
 * Created by haider on 29-04-2016.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class SmsDeliveredReceiver extends WakefulBroadcastReceiver {

    static public final String RESULT_CODE = "resultCode";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, SmsDeliveredService.class);
        service.putExtras(intent.getExtras());
        service.putExtra(RESULT_CODE, getResultCode());
        startWakefulService(context, service);
    }
}
