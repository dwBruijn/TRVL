package com.example.trvl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class CountryActivity extends AppCompatActivity {
    private View countryImage;
    private TextView countryName;
    private TextView introCountry;
    private TextView aboutCountry;
    private TextView countryAtAGlance;
    private TextView textCountryAtAGlance;
    private TextView textUniqueExperiences;
    private TextView textWeather;

    private String JWT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country);

        // get country name from intent
        Intent intent = getIntent();
        String country = intent.getStringExtra("country");

        // fetch JWT
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.prefJWTFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        this.JWT = pref.getString("JWT", "");

        this.countryImage = (View)findViewById(R.id.countryImage);
        this.countryName = (TextView)findViewById(R.id.countryName);
        this.introCountry = (TextView)findViewById(R.id.introCountry);
        this.aboutCountry = (TextView)findViewById(R.id.aboutCountry);
        this.countryAtAGlance = (TextView)findViewById(R.id.countryAtAGlance);
        this.textCountryAtAGlance = (TextView)findViewById(R.id.textCountryAtAGlance);
        this.textUniqueExperiences = (TextView)findViewById(R.id.textUniqueExpreinces);
        this.textWeather = (TextView)findViewById(R.id.textWeather);

        if(country.equals("Japan")) {
            this.countryImage.setBackgroundResource(R.drawable.japan);
            this.countryName.setText(R.string.japanCountry);
            this.introCountry.setText(R.string.introJapan);
            this.aboutCountry.setText(R.string.aboutJapan);
            this.countryAtAGlance.setText(R.string.japanAtAGlance);
            this.textCountryAtAGlance.setText(R.string.textJapanAtAGlance);
            this.textUniqueExperiences.setText(R.string.japanUniqueExpriences);
            this.textWeather.setText(R.string.japanWeather);
        }
    }

    public void inquire(View view) {

        // check if JWT exists
        if(this.JWT.equalsIgnoreCase("")) {
            // JWT doesn't exist
            // ask the user to login
            Intent intent1 = new Intent(CountryActivity.this, LoginActivity.class);
            // used by LoginActivity to know which activity to redirect the user to after successful login
            intent1.putExtra("nextActivity", "InquireActivity"); //Optional parameters
            startActivity(intent1);
            return;
        }

        // JWT exists so user is already logged in
        // validate JWT on the servr
        new CountryActivity.AsyncValidateJWT().execute(this.JWT);

        // JWT valid go to InquireActivity directly
        Intent myIntent = new Intent(CountryActivity.this, InquireActivity.class);
        CountryActivity.this.startActivity(myIntent);
    }

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
                url = new URL(getString(R.string.apiValidateJWTUrl));

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
                // Expired JWT
                // ask the user to login again
                Intent intent = new Intent(CountryActivity.this, LoginActivity.class);
                intent.putExtra("nextActivity", "InquireActivity"); //Optional parameters
                startActivity(intent);
                return;
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);
                msg = (String)jsonResponse.get("message");

                if(!msg.equalsIgnoreCase("Access granted.")) {
                    // to handle an extreme case where the response code is 200 but the token is invalid (should not happen)
                    Intent intent = new Intent(CountryActivity.this, LoginActivity.class);
                    intent.putExtra("nextActivity", "InquireActivity");
                    startActivity(intent);
                }

            } catch (JSONException e) {
                // in case of invalid response from server (should not happen)
                Log.e("ERROR", e.getMessage(), e);
                Intent intent = new Intent(CountryActivity.this, LoginActivity.class);
                intent.putExtra("nextActivity", "InquireActivity"); //Optional parameters
                startActivity(intent);
            }
        }
    }
}