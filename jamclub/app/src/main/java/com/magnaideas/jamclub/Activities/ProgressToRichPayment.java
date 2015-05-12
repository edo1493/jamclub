package com.magnaideas.jamclub.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.magnaideas.jamclub.R;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by edoardomoreni on 10/05/15.
 */
public class ProgressToRichPayment extends ActionBarActivity {
    public ProgressDialog prgDlg;
    private Double latitude;
    private Double longitude;
    private String address;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progresstorichpayment);
        mContext = getApplicationContext();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            address = extras.getString("address");
            latitude = extras.getDouble("latitude");
            longitude = extras.getDouble("longitude");
        }

        new LookingForUbers().execute();

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

        return super.onOptionsItemSelected(item);
    }

    private class LookingForUbers extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            prgDlg = ProgressDialog.show(ProgressToRichPayment.this, "Uber Search",
                    "We are checking if there are any Ubers", true);

        }

        @Override
        protected Void doInBackground(Void... args) {
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e2) {
                // TODO Auto-generated catch block
            }

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("latitude", latitude);
            params.put("longitude", longitude);
            ParseCloud.callFunctionInBackground("checkAvailability", params, new FunctionCallback<String>() {
                @Override
                public void done(String o, ParseException e) {

                    try {
                        JSONObject result = new JSONObject(o);
                        JSONArray products = result.getJSONArray("products");


                        if (products.length() > 0) {
                            // ok

                            // show currency on this page
                            JSONObject product = products.getJSONObject(0);
                            String currency_code = product.getJSONObject("price_details").getString("currency_code");

                            Intent intent = new Intent (mContext, RichPaymentActivity.class);
                            intent.putExtra("address", address);
                            intent.putExtra("latitude", latitude);
                            intent.putExtra("longitude", longitude);
                            intent.putExtra("currency", currency_code);
                            startActivity(intent);
                            finish();

                        } else {
                            // TODO: show "uber not available in this area"
                            Intent intent = new Intent();
                            setResult(ProgressToRichPayment.RESULT_OK, intent);
                            ProgressToRichPayment.this.finish();
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            if (prgDlg.isShowing())
                prgDlg.dismiss();
        }
    }
}
