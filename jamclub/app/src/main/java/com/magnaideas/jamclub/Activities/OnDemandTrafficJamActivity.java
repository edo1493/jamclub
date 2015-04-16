package com.magnaideas.jamclub.Activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.magnaideas.jamclub.R;

import Utils.LocationUpdates;

/**
 * Created by edoardomoreni on 11/04/2015.
 */
public class OnDemandTrafficJamActivity extends ActionBarActivity implements OnMapReadyCallback {

    private LocationUpdates mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ondemandtrafficjam);
        mLocation = new LocationUpdates(this);
        mLocation.buildGoogleApiClient();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap map) {

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        map.setLocationSource(mLocation);
        map.setMyLocationEnabled(true);

        Location mLocation = LocationUpdates.getLocation();

        if(mLocation != null) {
            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocation.mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocation.mGoogleApiClient.isConnected()) {
            mLocation.mGoogleApiClient.disconnect();
        }
    }

    public void richCarsButton (View v)
    {
        Intent intent = new Intent(this, RichPaymentActivity.class);
        startActivity(intent);
    }
}
