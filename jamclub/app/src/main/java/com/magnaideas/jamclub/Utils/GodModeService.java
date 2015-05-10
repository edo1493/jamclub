package com.magnaideas.jamclub.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by edoardomoreni on 07/05/15.
 */
public class GodModeService extends Service {

    private static final String TAG = "GodModeService";

    //private RetrieveLocations updateTask = new RetrieveLocations();
    private boolean doNotStartMoreTasks = false;

    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        doNotStartMoreTasks = false;
        new RetrieveLocations().execute();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        doNotStartMoreTasks = true;
        super.onDestroy();
    }

    public void onStart(Context context,Intent intent, int startId)
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private class RetrieveLocations extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            //Parse Query

            String response = "ciao";
            Log.d(TAG, "servicetask triggered");

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (! doNotStartMoreTasks)
                        new RetrieveLocations().execute();
                }
            }, 5000);
        }
    }
}

