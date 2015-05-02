package com.magnaideas.jamclub.Utils;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.magnaideas.jamclub.Activities.CallToArmsActivity;
import com.magnaideas.jamclub.Activities.EarnPaymentActivity;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by antoniomarino on 02/05/15.
 */
public class JamClubBroadcastReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        return super.getNotification(context, intent);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context,
                                                    Intent intent) {
        return CallToArmsActivity.class;
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

}