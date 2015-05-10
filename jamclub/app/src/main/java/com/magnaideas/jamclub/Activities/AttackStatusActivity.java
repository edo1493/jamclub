package com.magnaideas.jamclub.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.magnaideas.jamclub.R;
import com.magnaideas.jamclub.Utils.GodModeService;
import com.magnaideas.jamclub.Utils.LatLngInterpolator;
import com.magnaideas.jamclub.Utils.MarkerAnimation;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AttackStatusActivity extends ActionBarActivity implements
        OnMapReadyCallback {

    private static final String TAG = "AttackStatusActivity";
    private GoogleMap mMap;
    private double mLatitude;
    private double mLongitude;
    private TextView mAddress;
    private String attack_id;

    private ParseObject mAttack;

    //private Intent mServiceIntent;
    private Timer mTimer;
    private TimerTask mTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attack_status);

        SharedPreferences sharedPref = getSharedPreferences("attack", MODE_PRIVATE);
        String attack_id = sharedPref.getString("attack_id", null);

        Log.d(TAG, "" + attack_id);
        if (attack_id == null) {
            try {
                mTimerTask.cancel();
                mTimer.purge();
                mTimer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(this, OnDemandTrafficJamActivity.class);
            startActivity(intent);
            finish();
        }

        try {
            mAttack = new ParseQuery<>("Attack").get(attack_id);

            mAddress = (TextView)findViewById(R.id.address_attack);
            mAddress.setText(mAttack.getString("address"));
            mLatitude = mAttack.getDouble("latitude");
            mLongitude = mAttack.getDouble("longitude");


            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    timerTaskRun();
                }
            };


        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "An error has occurred", Toast.LENGTH_LONG).show();
        }
/*
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            attack_id = extras.getString("attack_id");
            mLatitude = extras.getDouble("latitude");
            mLongitude = extras.getDouble("longitude");
            mAddress.setText(extras.getString("address"));
        }
*/
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //mServiceIntent = new Intent(this, GodModeService.class);

    }

    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        LatLng position = new LatLng(mLatitude, mLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13));


        map.addMarker(new MarkerOptions().position(new LatLng(mLatitude, mLongitude))
                .icon(BitmapDescriptorFactory.fromBitmap(resize(getDrawable(R.drawable.pin)))));

        //startService(mServiceIntent);
    }

    private void timerTaskRun() {
        Log.d(TAG, "timertask triggered");

        ParseQuery query = new ParseQuery("Acceptance");
        query.whereEqualTo("attack_id", attack_id);
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, ParseException e) {
                Log.d(TAG, list.toString());
            }

            @Override
            public void done(Object o, Throwable throwable) {

            }
        });

    }

    private Bitmap resize(Drawable image) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 70, 180, true);
        return bitmapResized;
    }

    //this method wants marker and its final position
    public static void moveMarkers(Marker m, LatLng finalPosition)
    {
        LatLngInterpolator interpolator = new LatLngInterpolator.Linear();
        MarkerAnimation.animateMarkerToGB(m, finalPosition, interpolator);
    }

    @Override
    public void onDestroy() {
        mTimerTask.cancel();
        mTimer.purge();
        mTimer.cancel();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        //stopService(mServiceIntent);
        mTimerTask.cancel();
        mTimer.purge();

        super.onPause();
    }

    public void onResume() {
        super.onResume();
        //startService(mServiceIntent);
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                timerTaskRun();
            }
        };
        mTimer.scheduleAtFixedRate(mTimerTask, 0, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_attack_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_new_attack) {

            SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("attack_id");
            editor.commit();

            Intent intent = new Intent(this, OnDemandTrafficJamActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
