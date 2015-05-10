package com.magnaideas.jamclub.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.magnaideas.jamclub.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class OnDemandTrafficJamActivity extends ActionBarActivity implements
        OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    protected static final String TAG = "basic-location-sample";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String DETAILS_TYPE = "/details";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyDOTjD5hHyxIBLIiZfqIgW_UVmWtCUQv_k";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    private GoogleMap mMap;
    private TextView mAddress;
    private LinearLayout mButtonSearch;
    private LatLng mPosition;
    private Geocoder geocoder;
    private List<Address> addresses;
    private String mSelectedLocation;
    private boolean mSelectedLoc = false;
    private boolean mCameraPosition = false;
    private ImageView mPin;

    private static final int PICK_LOCATION = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        buildGoogleApiClient();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ondemandtrafficjam);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPin = (ImageView)findViewById(R.id.imageView1);
        mPin.setBackground(resize(getDrawable(R.drawable.pin)));

        mAddress = (TextView)findViewById(R.id.adressText);

        mButtonSearch = (LinearLayout)findViewById(R.id.buttonsearch);
        mButtonSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getApplicationContext(), SearchableActivity.class);
                startActivityForResult(intent, PICK_LOCATION);
            }

        });

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0){
                mPosition = mMap.getCameraPosition().target;

                try {
                    if(!mSelectedLoc) {
                        new GetLocationAsync(mPosition.latitude, mPosition.longitude)
                                .execute();
                    } else {
                        mAddress.setText(mSelectedLocation);
                        mSelectedLoc = false;
                    }

                } catch (Exception e) {
                }
            }

        });

    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            if(!mCameraPosition)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ondemandtrafficjamactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void richCarsButton (View v)
    {
        Intent intent = new Intent(this, RichPaymentActivity.class);
        intent.putExtra("address", mAddress.getText().toString());
        intent.putExtra("latitude", mPosition.latitude);
        intent.putExtra("longitude", mPosition.longitude);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_LOCATION) {

            if (resultCode == RESULT_OK) {
                mSelectedLoc = true;
                mCameraPosition = true;
                mSelectedLocation = data.getStringExtra("address");
                String place = data.getStringExtra("placeid");
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getLocationFromAddress(place), 13));

            }
        }
    }

    public LatLng getLocationFromAddress(String strAddress){
        String responseString = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + DETAILS_TYPE + OUT_JSON);
            sb.append("?placeid=" + URLEncoder.encode(strAddress, "utf8"));
            sb.append("&key=" + API_KEY);

            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(sb.toString()));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            }

        } catch (IOException e) {
                e.printStackTrace();
        }

        try {

            JSONObject jsonObj = new JSONObject(responseString);
            JSONObject jsonResult = jsonObj.getJSONObject("result");

            JSONObject jsonGeometry = jsonResult.getJSONObject("geometry");
            JSONObject jsonLocation = jsonGeometry.getJSONObject("location");
            System.out.println(jsonLocation.toString());

            return new LatLng(jsonLocation.getDouble("lat"), jsonLocation.getDouble("lng"));


        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return null;
    }

    private class GetLocationAsync extends AsyncTask<String, Void, String> {

        double x, y;
        StringBuilder str;

        public GetLocationAsync(double latitude, double longitude) {
            // TODO Auto-generated constructor stub
            x = latitude;
            y = longitude;
        }

        @Override
        protected void onPreExecute() {
            mAddress.setText(" Getting location ");
        }

        @Override
        protected String doInBackground(String... params) {

            try {

                geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
                addresses = geocoder.getFromLocation(x, y, 1);
                str = new StringBuilder();
                if (geocoder.isPresent()) {
                    return addresses.get(0).getAddressLine(0);
                }
            } catch (IOException e) {
                Log.e("tag", e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            try {
                mAddress.setText(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 70, 180, true);
        return new BitmapDrawable(getResources(), bitmapResized);
    }
}

