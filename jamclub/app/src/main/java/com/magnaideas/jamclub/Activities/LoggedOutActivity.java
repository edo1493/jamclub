package com.magnaideas.jamclub.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.magnaideas.jamclub.R;

/**
 * Created by edoardomoreni on 11/04/2015.
 */
public class LoggedOutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loggedout);
    }

    private void richLogin(View v)
    {
        Intent intent = new Intent(this, OnDemandTrafficJamActivity.class);
        startActivity(intent);

    }

    private void poorLogin(View v)
    {
        Intent intent = new Intent(this, EarnLoginActivity.class);
        startActivity(intent);

    }


}
