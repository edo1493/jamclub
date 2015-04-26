package com.magnaideas.jamclub.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.magnaideas.jamclub.R;

/**
 * Created by edoardomoreni on 11/04/2015.
 */
public class EarnLoginActivity extends Activity {

    private WebView mUberLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*mUberLogin = new WebView(this);
        mUberLogin.loadUrl("https://login.uber.com/login");*/
        setContentView(R.layout.activity_earnlogin);
    }

    public void earnLogin (View v) {
        /*AlertDialog.Builder alert = new AlertDialog.Builder(this);
        mUberLogin.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        alert.setView(mUberLogin);

        alert.show();*/
        Intent intent = new Intent(this, UberLoginActivity.class);
        startActivity(intent);

    }

}
