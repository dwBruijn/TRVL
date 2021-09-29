package com.example.trvl;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
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

public class InquireActivity extends AppCompatActivity {


    private Spinner destination;
    private EditText travelDate;
    private EditText tripLength;
    private EditText numberOfPersons;
    private RadioButton tripTypeCustom;
    private RadioButton tripTypeGroupTour;
    private RadioButton tripTypeUpToAgent;
    private RadioButton accommodationLevel5;
    private RadioButton accommodationLevel4;
    private RadioButton accommodationLevel3;
    private EditText numberOfHotelRooms;
    private CheckBox servActivities;
    private CheckBox servInterFlights;
    private CheckBox servInCountryTransportation;
    private CheckBox servTourGuide;
    private EditText requestedActivities;
    private EditText budgetPerPerson;
    private RadioButton budgetInterFlightsYes;
    private RadioButton budgetInterFightsNo;
    private RadioButton budgetFlexibilityLow;
    private RadioButton budgetFlexibilityMedium;
    private RadioButton budgetFlexibilityHigh;
    private EditText specificRequests;

    private TextView inquireResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquire);


        this.destination = (Spinner)findViewById(R.id.destination);
        // init spinner country
        ArrayAdapter<CharSequence> cAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
        cAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        destination.setAdapter(cAdapter);
        destination.setSelection(0);

        this.travelDate = (EditText) findViewById(R.id.travelDate);
        this.tripLength = (EditText) findViewById(R.id.tripLength);
        this.numberOfPersons = (EditText) findViewById(R.id.numberOfPersons);
        this.tripTypeCustom = (RadioButton) findViewById(R.id.tripTypeCustom);
        this.tripTypeGroupTour = (RadioButton) findViewById(R.id.tripTypeGroupTour);
        this.tripTypeUpToAgent = (RadioButton) findViewById(R.id.tripTypeUpToAgent);
        this.accommodationLevel5 = (RadioButton) findViewById(R.id.accommodationLevel5);
        this.accommodationLevel4 = (RadioButton) findViewById(R.id.accommodationLevel4);
        this.accommodationLevel3 = (RadioButton) findViewById(R.id.accommodationLevel3);
        this.numberOfHotelRooms = (EditText) findViewById(R.id.numberOfHotelRooms);
        this.servActivities = (CheckBox) findViewById(R.id.serviceActivities);
        this.servInterFlights = (CheckBox) findViewById(R.id.serviceInternationFlights);
        this.servInCountryTransportation = (CheckBox) findViewById(R.id.serviceInCountryTransportation);
        this.servTourGuide = (CheckBox) findViewById(R.id.serviceTourGuide);
        this.requestedActivities = (EditText) findViewById(R.id.requestedActivities);
        this.budgetPerPerson = (EditText) findViewById(R.id.budgetPerPerson);
        this.budgetInterFlightsYes = (RadioButton) findViewById(R.id.budgetInterFlightsYes);
        this.budgetInterFightsNo = (RadioButton) findViewById(R.id.budgetInterFlightsNo);
        this.budgetFlexibilityLow = (RadioButton) findViewById(R.id.budgetFlexibilityLow);
        this.budgetFlexibilityMedium = (RadioButton) findViewById(R.id.budgetFlexibilityMedium);
        this.budgetFlexibilityHigh = (RadioButton) findViewById(R.id.budgetFlexibilityHigh);
        this.specificRequests = (EditText) findViewById(R.id.specificRequests);
        this.inquireResponse = (TextView) findViewById(R.id.inquireResponse);

        this.tripTypeCustom.setChecked(true);
        this.accommodationLevel5.setChecked(true);
        this.budgetInterFlightsYes.setChecked(true);
        this.budgetFlexibilityLow.setChecked(true);

    }

    public void sendInquiry(View view) {
        // check JWT
        // if not valid redirect to login
        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.prefJWTFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        String JWT = pref.getString("JWT", "");

        if(JWT.equalsIgnoreCase("")) {
            // JWT doesn't exist
            // ask the user to login
            Intent intent = new Intent(InquireActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        // validate JWT on the servr
        new AsyncValidateJWT().execute(JWT);

        if(
                this.destination.getSelectedItem().toString().equalsIgnoreCase("Country") ||
                this.travelDate.getText().toString().equals("") ||
                this.tripLength.getText().toString().equals("") ||
                this.numberOfPersons.getText().toString().equals("") ||
                this.numberOfHotelRooms.getText().toString().equals("") ||
                this.requestedActivities.getText().toString().equals("") ||
                this.budgetPerPerson.getText().toString().equals("") ||
                this.specificRequests.getText().toString().equals("")
        ) {
            this.inquireResponse.setTextColor(Color.RED);
            this.inquireResponse.setText("Some field(s) are emtpy");
            return;
        }

        String country = this.destination.getSelectedItem().toString();
        String travelDate = this.travelDate.getText().toString();
        String tripLength = this.tripLength.getText().toString();
        String numberOfPersons = this.numberOfPersons.getText().toString();
        String tripType = "";
        if(this.tripTypeCustom.isChecked()) {
            tripType = "custom trip";
        } else if(this.tripTypeGroupTour.isChecked()) {
            tripType = "group tour";
        } else if(this.tripTypeUpToAgent.isChecked()) {
            tripType = "up to agent";
        } else {
            tripType = "";
        }
        String accommodationLevel = "";
        if(this.accommodationLevel5.isChecked()) {
            accommodationLevel = "5";
        } else if(this.accommodationLevel4.isChecked()) {
            accommodationLevel = "4";
        } else if(this.accommodationLevel3.isChecked()) {
            accommodationLevel = "3";
        } else {
            accommodationLevel = "";
        }
        String numberOfHotelRooms = this.numberOfHotelRooms.getText().toString();
        String servActivities = "0";
        String servInterFlights = "0";
        String servInCountryTransportation = "0";
        String servTourGuide = "0";
        if(this.servActivities.isChecked()) {
            servActivities = "1";
        }
        if(this.servInterFlights.isChecked()) {
            servInterFlights = "1";
        }
        if(this.servInCountryTransportation.isChecked()) {
            servInCountryTransportation = "1";
        }
        if(this.servTourGuide.isChecked()) {
            servTourGuide = "1";
        }
        String requestedActivities = this.requestedActivities.getText().toString();
        String budgetPerPerson = this.budgetPerPerson.getText().toString();
        String budgetInterFlights = "";
        if(this.budgetInterFlightsYes.isChecked()) {
            budgetInterFlights = "true";
        } else {
            budgetInterFlights = "false";
        }
        String budgetFlexibility = "";
        if(this.budgetFlexibilityLow.isChecked()) {
            budgetFlexibility = "low";
        } else if(this.budgetFlexibilityMedium.isChecked()) {
            budgetFlexibility = "medium";
        } else if(this.budgetFlexibilityHigh.isChecked()) {
            budgetFlexibility = "high";
        } else {
            budgetFlexibility = "";
        }
        String specificRequests = this.specificRequests.getText().toString();

        new AsyncSubmitInquiry().execute(
                country,
                travelDate,
                tripLength,
                numberOfPersons,
                tripType,
                accommodationLevel,
                numberOfHotelRooms,
                servActivities,
                servInterFlights,
                servInCountryTransportation,
                servTourGuide,
                requestedActivities,
                budgetPerPerson,
                budgetInterFlights,
                budgetFlexibility,
                specificRequests,
                JWT
        );
    }

    public class AsyncValidateJWT extends AsyncTask<String, String, String> {
        public static final int CONNECTION_TIMEOUT=10000;
        public static final int READ_TIMEOUT=15000;

        HttpURLConnection conn;
        URL url = null;

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
                conn.setReadTimeout(AsyncValidateJWT.READ_TIMEOUT);
                conn.setConnectTimeout(AsyncValidateJWT.CONNECTION_TIMEOUT);
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

            if(result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception")) {
                InquireActivity.this.inquireResponse.setText("Something went wrong");
                return;
            } else if(result.equalsIgnoreCase("unauthorized")) {
                // the JWT has expired
                // ask the user to login again
                Intent intent = new Intent(InquireActivity.this, LoginActivity.class);
                intent.putExtra("nextActivity", "InquireActivity"); //Optional parameters
                startActivity(intent);
                finish();
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);
                msg = (String)jsonResponse.get("message");

                if(!msg.equalsIgnoreCase("Access granted.")) {
                    // to handle an extreme case where the response code is 200 but the token is invalid (should not happen)
                    Intent intent = new Intent(InquireActivity.this, LoginActivity.class);
                    intent.putExtra("nextActivity", "InquireActivity"); //Optional parameters
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                // in case of invalid response from server
                Log.e("ERROR", e.getMessage(), e);
                InquireActivity.this.inquireResponse.setText("Something went wrong");
                return;
            }
        }
    }

    private class AsyncSubmitInquiry extends AsyncTask<String, String, String> {
        public static final int CONNECTION_TIMEOUT=10000;
        public static final int READ_TIMEOUT=15000;

        ProgressDialog pdLoading = new ProgressDialog(InquireActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {

            JSONObject jsonObj = new JSONObject();
            String jsonData = "";

            // fields data
            String country = params[0];
            String travelDate = params[1];
            String tripLength = params[2];
            String numberOfPersons = params[3];
            String tripType = params[4];
            String accommodationLevel = params[5];
            String numberOfHotelRooms = params[6];
            String servActivities = params[7];
            String servInterFlights = params[8];
            String servInCountryTransportation = params[9];
            String servTourGuide = params[10];
            String requestedActivities = params[11];
            String budgetPerPerson = params[12];
            String budgetInterFlights = params[13];
            String budgetFlexibility = params[14];
            String specificRequests = params[15];
            String jwt = params[16];

            // add data to json object
            try {
                jsonObj.put("country", country);
                jsonObj.put("travel_date", travelDate);
                jsonObj.put("trip_length", tripLength);
                jsonObj.put("number_of_persons", numberOfPersons);
                jsonObj.put("trip_type", tripType);
                jsonObj.put("level_of_accommodation", accommodationLevel);
                jsonObj.put("number_of_hotel_rooms", numberOfHotelRooms);
                jsonObj.put("serv_activities", servActivities);
                jsonObj.put("serv_interflights", servInterFlights);
                jsonObj.put("serv_incountry_transportation", servInCountryTransportation);
                jsonObj.put("serv_tour_guide", servTourGuide);
                jsonObj.put("requested_activities", requestedActivities);
                jsonObj.put("budget_per_person", budgetPerPerson);
                jsonObj.put("budget_inter_flights", budgetInterFlights);
                jsonObj.put("budget_flexibility", budgetFlexibility);
                jsonObj.put("specific_requests", specificRequests);
                jsonObj.put("jwt", jwt);

            } catch (JSONException e) {
                // catch JSONException
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // URL for login API endpoint
                url = new URL(getString(R.string.serverIP) + getString(R.string.apiCreateInquiryUrl));

            } catch (MalformedURLException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(AsyncSubmitInquiry.READ_TIMEOUT);
                conn.setConnectTimeout(AsyncSubmitInquiry.CONNECTION_TIMEOUT);
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
                // if success or failure read response json
                if (response_code == HttpURLConnection.HTTP_OK) {
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    input.close();
                    reader.close();
                    // Pass data to onPostExecute method
                    return (result.toString());

                } else if(response_code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Read data sent from server
                    InputStream input = conn.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    input.close();
                    reader.close();
                    // Pass data to onPostExecute method
                    return (result.toString());
                } else {
                    // Failed to create account due to an unknown error
                    return ("error");
                }
            } catch (IOException e) {
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            pdLoading.dismiss();

            // to hold the json data returned by the server
            JSONObject jsonResponse;

            // notify user that an unknown error occured
            if (result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception")) {
                InquireActivity.this.inquireResponse.setTextColor(Color.RED);
                InquireActivity.this.inquireResponse.setText(R.string.signupRespError);
                return;
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);

                if(jsonResponse.get("error").toString().equalsIgnoreCase("")) {
                    // success
                    InquireActivity.this.inquireResponse.setTextColor(Color.BLACK);
                    InquireActivity.this.inquireResponse.setText("Inquiry Sumitted");

                } else {
                    // display error
                    InquireActivity.this.inquireResponse.setTextColor(Color.RED);
                    InquireActivity.this.inquireResponse.setText(jsonResponse.get("error").toString());
                }
            } catch (JSONException e) {
                // in case of invalid response from server
                Log.e("ERROR", e.getMessage(), e);
                InquireActivity.this.inquireResponse.setText(R.string.loginRespError);
            }
        }
    }
}