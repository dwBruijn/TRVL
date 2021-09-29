package com.example.trvl;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AccountHomeActivity extends AppCompatActivity {
    private static LinearLayout accountHomeLayout;
    private static TextView inquiryActionResult;
    String JWT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_home);

        AccountHomeActivity.accountHomeLayout = (LinearLayout)findViewById(R.id.accountHomeLayout);
        AccountHomeActivity.inquiryActionResult = (TextView)findViewById(R.id.textViewInquiryActionResult);

        // check JWT
        // if not valid redirect to login
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.prefJWTFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        this.JWT = pref.getString("JWT", "");
        if(this.JWT.equalsIgnoreCase("")) {
            // JWT doesn't exist
            // ask the user to login
            Intent intent = new Intent(AccountHomeActivity.this, LoginActivity.class);
            intent.putExtra("nextActivity", "AccountHome"); //Optional parameters
            startActivity(intent);
            finish();
            return;
        }
        // validate JWT on the servr
        new AccountHomeActivity.AsyncValidateJWT().execute(this.JWT);

        // fetch user's inquiries
        new AccountHomeActivity.AsyncGetUserInquiries().execute(this.JWT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);

        MenuItem item = menu.findItem(R.id.actionbar_menu);

        // hide profile_icon option
        menu.findItem(R.id.menu_profile_icon).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    // when logout_icon is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_action_logout) {
            // destroy JWT
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.prefJWTFile), Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.savedJWT), "");
            editor.apply();

            // send user to MainActivity
            Intent intent = new Intent(AccountHomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // used to validate JWT
    public class AsyncValidateJWT extends AsyncTask<String, String, String>
    {
        HttpURLConnection conn;
        URL url = null;

        public static final int CONNECTION_TIMEOUT=10000;
        public static final int READ_TIMEOUT=15000;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            JSONObject jsonObj = new JSONObject();
            String jsonData = "";
            String jwt = params[0];

            // add data to json object
            try {
                jsonObj.put("jwt", jwt);
            } catch(JSONException e) {
                // catch JSONException
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // URL for validate_token API endpoint
                url = new URL(getString(R.string.serverIP) + getString(R.string.apiValidateJWTUrl));

            } catch (MalformedURLException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // prepare data and open connection for sending data
                jsonData = jsonObj.toString();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));

                writer.write(jsonData, 0, jsonData.length());

                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                } if(response_code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // invalid JWT
                    return ("unauthorized");
                } else {
                    // Unknown error occured
                    return("error");
                }
            } catch (IOException e) {
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            JSONObject jsonResponse;
            String msg = "";

            if(result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unauthorized")) {
                // ask the user to login again
                Intent intent = new Intent(AccountHomeActivity.this, LoginActivity.class);
                intent.putExtra("nextActivity", "AccountHome"); //Optional parameters
                startActivity(intent);
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);
                msg = (String)jsonResponse.get("message");

                if(!msg.equalsIgnoreCase("Access granted.")) {
                    // to handle an extreme case where the response code is 200 but the token is invalid (should not happen)
                    Intent intent = new Intent(AccountHomeActivity.this, LoginActivity.class);
                    intent.putExtra("nextActivity", "AccountHome");
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                // in case of invalid response from server (should not happen)
                Log.e("ERROR", e.getMessage(), e);
                Intent intent = new Intent(AccountHomeActivity.this, LoginActivity.class);
                intent.putExtra("nextActivity", "AccountHome"); //Optional parameters
                startActivity(intent);
                finish();
            }
        }
    }

    // fetch the inquries of the user
    public class AsyncGetUserInquiries extends AsyncTask<String, String, String>
    {
        HttpURLConnection conn;
        URL url = null;

        public static final int CONNECTION_TIMEOUT=10000;
        public static final int READ_TIMEOUT=15000;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            JSONObject jsonObj = new JSONObject();
            String jsonData = "";
            String jwt = params[0];

            // add data to json object
            try {
                jsonObj.put("jwt", jwt);
            } catch(JSONException e) {
                // catch JSONException
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // URL for validate_token API endpoint
                url = new URL(getString(R.string.serverIP) + getString(R.string.apiGetUserInquiriesUrl));

            } catch (MalformedURLException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // prepare data and open connection for sending data
                jsonData = jsonObj.toString();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));

                writer.write(jsonData, 0, jsonData.length());

                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                } if(response_code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // invalid JWT
                    return ("unauthorized");
                } else {
                    // Unknown error occured
                    return("error");
                }
            } catch (IOException e) {
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {

            JSONObject jsonResponse;
            String msg = "";

            if(result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unauthorized")) {
                // fetching inquiries failed
                // ask the user to login again
                Intent intent = new Intent(AccountHomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            // parse response json
            try {
                // if the user has no inquiries yet
                if(result.equalsIgnoreCase("[]")) {
                    return;
                }

                jsonResponse = new JSONObject(result);
                JSONArray keys = jsonResponse.names();
                String value = "";
                List<String> inquiryData;

                // to dynamiclally construct CardViews
                LayoutInflater inflater = getLayoutInflater();
                View cardInquiry;

                String destination = "";
                String date = "";
                String agent = "";
                String status = "";

                // loop over each fetched inquiry and inflate a CardView for it
                // then add that inquiry's data to its CardView
                for (int i = 0; i < keys.length(); i++) {
                    String inquiryId = keys.getString(i);
                    value = jsonResponse.getString(inquiryId);
                    inquiryData = Arrays.asList(value.split("\""));

                    destination = inquiryData.get(1);
                    date = inquiryData.get(3);
                    status = inquiryData.get(5);
                    agent = inquiryData.get(7);

                    // create inquiry CardView
                    cardInquiry = inflater.inflate(R.layout.cardview_inquiry, AccountHomeActivity.accountHomeLayout, false);

                    // add inquiry data to the inflated CardView
                    TextView textViewDestination = (TextView)cardInquiry.findViewById(R.id.textViewInquiryDestination);
                    textViewDestination.setText(textViewDestination.getText().toString() + destination);
                    TextView textViewDate = cardInquiry.findViewById(R.id.textViewInquiryDate);
                    textViewDate.setText(textViewDate.getText().toString() + date.substring(0, 10));
                    TextView textViewAgent = cardInquiry.findViewById(R.id.textViewInquiryAgent);
                    textViewAgent.setText(textViewAgent.getText().toString() + agent);
                    TextView textViewStatus = cardInquiry.findViewById(R.id.textViewInquiryStatus);
                    textViewStatus.setText(textViewStatus.getText().toString() + status);

                    // set checkOffer and deleteInquiry btn id to inquiryId to know which button was clicked
                    Button checkOffer = (Button)cardInquiry.findViewById(R.id.btnCheckOffer);
                    Button deleteOffer = (Button)cardInquiry.findViewById(R.id.btnDeleteInquiry);
                    checkOffer.setId(Integer.parseInt(inquiryId));
                    deleteOffer.setId(Integer.parseInt(inquiryId));

                    // set buttons state
                    if(status.equalsIgnoreCase("awaiting offer") || status.equalsIgnoreCase("awaiting updated offer")) {
                        checkOffer.setEnabled(false);
                    } else if(status.equalsIgnoreCase("offer accepted")) {
                        deleteOffer.setEnabled(false);
                    }

                    // add the infalted CardView to the LinearLayout
                    AccountHomeActivity.accountHomeLayout.addView(cardInquiry);

                }
            } catch (JSONException e) {
                // in case of invalid response from server (should not happen)
                Log.e("ERROR", e.getMessage(), e);
                Intent intent = new Intent(AccountHomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }

    public void CheckOffer(View view) {
        // get id of the clicked button to detect which inquiry the user is interacting with
        int id = view.getId();

        Intent intent = new Intent(AccountHomeActivity.this, OfferActivity.class);
        intent.putExtra("inquiryId", ""+id);
        AccountHomeActivity.this.startActivity(intent);
    }

    public void deleteInquiry(View view) {
        int inquiryId = view.getId();
        new AccountHomeActivity.AsyncDeleteInquiry().execute(this.JWT, ""+inquiryId);
        return;
    }

    public class AsyncDeleteInquiry extends AsyncTask<String, String, String>
    {
        HttpURLConnection conn;
        URL url = null;

        public static final int CONNECTION_TIMEOUT=10000;
        public static final int READ_TIMEOUT=15000;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            JSONObject jsonObj = new JSONObject();
            String jsonData = "";

            String jwt = params[0];
            String inquiryId = params[1];

            // add data to json object
            try {
                jsonObj.put("jwt", jwt);
                jsonObj.put("id", inquiryId);
            } catch(JSONException e) {
                // catch JSONException
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // URL for validate_token API endpoint
                url = new URL(getString(R.string.serverIP) + getString(R.string.apiDeleteInquiryUrl));

            } catch (MalformedURLException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // prepare data and open connection for sending data
                jsonData = jsonObj.toString();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));

                writer.write(jsonData, 0, jsonData.length());

                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return(result.toString());

                } if(response_code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // invalid JWT
                    return ("unauthorized");
                } else {
                    // Unknown error occured
                    return("error");
                }
            } catch (IOException e) {
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {

            JSONObject jsonResponse;
            String msg = "";

            if(result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unauthorized")) {
                // failed to delete inquiry
                Toast.makeText(AccountHomeActivity.this, "Failed to delete inquiry.", Toast.LENGTH_LONG).show();
                return;
            }

            // reload activity
            finish();
            startActivity(getIntent());
        }
    }
}