package com.magnaideas.jamclub.Activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.magnaideas.jamclub.R;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;

import static com.magnaideas.jamclub.R.id.button;

/**
 * Created by edoardomoreni on 11/04/2015.
 */
public class RichPaymentActivity extends ActionBarActivity {

    private static final String TAG = "RichPaymentActivity";
    private TextView mLatitude;
    private TextView mLongitude;
    private double latitude;
    private double longitude;
    private String address;

    /**
     * - Set to PayPalConfiguration.ENVIRONMENT_PRODUCTION to move real money.
     *
     * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     *
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "AYimypdxdb4VUaOWk5RRy0hPMcZu1jAvkC1U5TTn79oJsWkl7U92AkXhPsGoSGa5uVR5xsUeu2Jdrq_F";

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final int REQUEST_CODE_PROFILE_SHARING = 3;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
                    // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("Example Merchant")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_richpayment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            address = extras.getString("address");
            latitude = extras.getDouble("latitude");
            longitude = extras.getDouble("longitude");
        }

        mLatitude = (TextView)findViewById(R.id.latitude);
        mLatitude.setText(" " + latitude);

        mLongitude = (TextView)findViewById(R.id.longitude);
        mLongitude.setText(" " + longitude);

        final Button payButton = (Button) findViewById(R.id.button);
        payButton.setEnabled(false);

        final EditText budgetEdit = (EditText) findViewById(R.id.budget);
        budgetEdit.setEnabled(false);

        final TextView currencyTextView = (TextView)findViewById(R.id.currency);


        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        ParseCloud.callFunctionInBackground("checkAvailability", params, new FunctionCallback<String>() {
            @Override
            public void done(String o, ParseException e) {
                Log.d(TAG, o);

                try {
                    JSONObject result = new JSONObject(o);
                    Log.d(TAG, result.toString());
                    JSONArray products = result.getJSONArray("products");
                    Log.d(TAG, products.toString());
                    if (products.length() > 0) {
                        // ok

                        // show currency on this page
                        JSONObject product = products.getJSONObject(0);
                        String currency_code = product.getJSONObject("price_details").getString("currency_code");
                        Log.d(TAG, currency_code);
                        currencyTextView.setText(" " + currency_code);

                        budgetEdit.setEnabled(true);
                        payButton.setEnabled(true);

                    } else {
                        // TODO: show "uber not available in this area"
                        currencyTextView.setText("UBER is not available in this area.");

                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public void pay(View view) {

        EditText budgetEditText = (EditText) findViewById(R.id.budget);
        String budgetString = budgetEditText.getText().toString();
        BigDecimal budget;
        try {
            budget = new BigDecimal(budgetString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Not a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
        PayPalPayment payPalPayment = new PayPalPayment(
                budget,
                "USD",
                "JamClub",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);

        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);

        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    Log.i("paymentExample", confirm.toJSONObject().toString(4));

                    // TODO: send 'confirm' to your server for verification.
                    // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                    // for more details.

                    afterPayment(confirm, null);

                } catch (JSONException e) {
                    Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                }
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("paymentExample", "The user canceled.");
        }
        else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
        }
    }

    private void afterPayment(PaymentConfirmation confirm, BigDecimal budget) {
        ParseObject attack = new ParseObject("Attack");
        attack.put("attacker", ParseUser.getCurrentUser());
        if (confirm != null) {
            attack.put("payment_info", confirm.getPayment().toJSONObject());
            attack.put("payment_confirmation", confirm.toJSONObject());
        }
        //attack.put("address", address);
        attack.put("latitude", latitude);
        attack.put("longitude", longitude);
        if (budget != null) attack.put("budget", budget);
        attack.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Intent intent = new Intent(getApplicationContext(), AttackStatusActivity.class);
                intent.putExtra("address", address);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_richpaymentactivity, menu);
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

        if (id == R.id.skip_payment) {
            EditText budgetEditText = (EditText) findViewById(R.id.budget);
            String budgetString = budgetEditText.getText().toString();
            BigDecimal budget;
            try {
                budget = new BigDecimal(budgetString);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Not a valid amount", Toast.LENGTH_SHORT).show();
                return false;
            }
            afterPayment(null, budget);
        }

        return super.onOptionsItemSelected(item);
    }
}
