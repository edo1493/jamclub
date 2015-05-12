package com.magnaideas.jamclub.Activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.magnaideas.jamclub.R;
import com.magnaideas.jamclub.Utils.CustomTitle;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CallToArmsActivity extends ActionBarActivity {

    private static final String TAG = "CallToArmsActivity";
    private String attack_id = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_to_arms);
        getSupportActionBar().hide();

        // TODO: check if attack is still ongoing
        try {
            JSONObject json = new JSONObject(getIntent().getExtras().getString("com.parse.Data"));
            Log.d(TAG, json.toString());
            attack_id = json.getString("attack_id");
            Log.d(TAG, attack_id);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void accept(View view) {
        // send acceptance to server - associate with attack id

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Attack");
        query.getInBackground(attack_id, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    ParseObject accepted = new ParseObject("Acceptance");
                    accepted.put("user", ParseUser.getCurrentUser());
                    accepted.put("attack", object);
                    accepted.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                try {
                                    LinearLayout linear1 = (LinearLayout)findViewById(R.id.linear1);
                                    LinearLayout linear2 = (LinearLayout)findViewById(R.id.accept_decline);

                                    linear1.setVisibility(View.GONE);
                                    linear2.setVisibility(View.GONE);

                                    CustomTitle mThanks = (CustomTitle)findViewById(R.id.thanks);
                                    mThanks.setVisibility(View.VISIBLE);
                                    Thread.sleep(2000);
                                    finish();
                                } catch (InterruptedException e2) {
                                    // TODO Auto-generated catch block
                                }

                            } else {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });

                } else {
                    // something went wrong
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    public void decline(View view) {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_call_to_arms, menu);
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
}
