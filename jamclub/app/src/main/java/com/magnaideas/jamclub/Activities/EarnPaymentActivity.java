package com.magnaideas.jamclub.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.magnaideas.jamclub.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by edoardomoreni on 11/04/2015.
 */
public class EarnPaymentActivity extends ActionBarActivity {

    private static final String TAG = "EarnPaymentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earnpayment);

        ParseUser user = ParseUser.getCurrentUser();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText editText = (EditText) findViewById(R.id.editText);
        String prefill = ( user.getString("paypal_address") != null )
                ? user.getString("paypal_address") : user.getEmail();
        editText.setText(prefill);

    }

    public void submitEmail(View v) {

        EditText editText = (EditText) findViewById(R.id.editText);
        String paypalAddress = editText.getText().toString();

        if ( paypalAddress.isEmpty() || ! Patterns.EMAIL_ADDRESS.matcher(paypalAddress).matches() ) {
            Toast.makeText(this, "Not a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        editText.setEnabled(false);

        final Button button = (Button) findViewById(R.id.button2);
        button.setEnabled(false);

        ParseUser user = ParseUser.getCurrentUser();
        user.put("paypal_address", paypalAddress);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                button.setText("Saved");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_earnpaymentactivity, menu);
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
}
