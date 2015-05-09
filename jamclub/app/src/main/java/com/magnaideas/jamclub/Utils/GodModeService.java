package com.magnaideas.jamclub.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;

/**
 * Created by edoardomoreni on 07/05/15.
 */
public class GodModeService extends Service {

    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        new retrieveLocations().execute();

        return START_STICKY;
    }

    public void onStart(Context context,Intent intent, int startId)
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private class retrieveLocations extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            //Parse Query

            String response = "ciao";

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    new retrieveLocations().execute();
                }
            }, 5000);
        }
    }
}

