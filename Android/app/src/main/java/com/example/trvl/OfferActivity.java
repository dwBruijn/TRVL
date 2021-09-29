package com.example.trvl;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OfferActivity extends AppCompatActivity {
    private int offerId;
    private TextView flightType;
    private TextView flightAccommodation;
    private TextView ticketPrice;
    private TextView groundTranspPrice;
    private TextView hotelName;
    private TextView hotelAddress;
    private TextView numberOfHotelRooms;
    private TextView roomPricePerNight;
    private TextView tourGuide;
    private TextView tourGuidePrice;
    private TextView activitiesDetails;
    private TextView activitiesPricePerPerson;
    private TextView generalSuggestions;
    private TextView totalPrice;
    private Button offerAccept;
    private Button offerDecline;
    private TextView checkOfferResponse;
    private EditText requestedChanges;
    private Button submitRequestedChanges;
    private String JWT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer);

        // get clicked inquiry ID
        Intent intent = getIntent();
        String inquiryID = intent.getStringExtra("inquiryId");

        SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.prefJWTFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        this.JWT = pref.getString("JWT", "");
        if(this.JWT.equalsIgnoreCase("")) {
            // JWT doesn't exist
            // ask the user to login
            Intent intent1 = new Intent(OfferActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        // validate JWT on the servr
        new OfferActivity.AsyncValidateJWT().execute(this.JWT);

        // init views objects
        this.flightType = (TextView)findViewById(R.id.offerFlightType);
        this.flightAccommodation = (TextView)findViewById(R.id.offerFlightAccommodation);
        this.ticketPrice = (TextView)findViewById(R.id.offerTicketPrice);
        this.groundTranspPrice = (TextView)findViewById(R.id.offerGroundTranspPrice);
        this.hotelName = (TextView)findViewById(R.id.offerHotelName);
        this.hotelAddress = (TextView)findViewById(R.id.offerHotelAddress);
        this.numberOfHotelRooms = (TextView)findViewById(R.id.OfferNumberOfHotelRooms);
        this.roomPricePerNight = (TextView)findViewById(R.id.offerRoomPricePerNight);
        this.tourGuide = (TextView)findViewById(R.id.offerTourGuide);
        this.tourGuidePrice = (TextView)findViewById(R.id.offerTourGuidePrice);
        this.activitiesDetails = (TextView)findViewById(R.id.offerActivitiesDetails);
        this.activitiesPricePerPerson = (TextView)findViewById(R.id.offerActivitiesPricePerPerson);
        this.generalSuggestions = (TextView)findViewById(R.id.offerGeneralSuggestions);
        this.totalPrice = (TextView)findViewById(R.id.offerTotalPrice);
        this.offerAccept = (Button) findViewById(R.id.offerBtnAccept);
        this.offerDecline = (Button) findViewById(R.id.offerBtnDecline);
        this.checkOfferResponse = (TextView)findViewById(R.id.checkOfferResponse);
        this.requestedChanges = (EditText)findViewById(R.id.requestedChanges);
        this.submitRequestedChanges = (Button)findViewById(R.id.offerBtnSubmitRequestedChanges);

        new OfferActivity.AsyncCheckOffer().execute(inquiryID);
    }

    public void acceptOffer(View view) {
        new OfferActivity.AsyncAcceptOffer().execute(this.JWT, ""+this.offerId);
    }

    public void declineOffer(View view) {
        this.requestedChanges.setVisibility(View.VISIBLE);
        this.submitRequestedChanges.setVisibility(View.VISIBLE);
        // focus on edittext and open keyboard
        this.requestedChanges.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.requestedChanges, InputMethodManager.SHOW_IMPLICIT);
    }

    public void submitRequestedChanges(View view) {
        String requestedChanges = this.requestedChanges.getText().toString();
        // if requestedChanges field is empty
        if(requestedChanges.equalsIgnoreCase("")) {
            this.checkOfferResponse.setText(R.string.offerEmptyRequestedChangesField);
            return;
        }
        new OfferActivity.AsyncDeclineOffer().execute(this.JWT, ""+this.offerId, requestedChanges);
        this.submitRequestedChanges.setEnabled(false);
    }

    private class AsyncValidateJWT extends AsyncTask<String, String, String>
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
                Intent intent = new Intent(OfferActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);
                msg = (String)jsonResponse.get("message");

                if(!msg.equalsIgnoreCase("Access granted.")) {
                    // to handle an extreme case where the response code is 200 but the token is invalid (should not happen)
                    Intent intent = new Intent(OfferActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            } catch (JSONException e) {
                // in case of invalid response from server (should not happen)
                Log.e("ERROR", e.getMessage(), e);
                Intent intent = new Intent(OfferActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }

    private class AsyncCheckOffer extends AsyncTask<String, String, String> {
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
            String inquiryId = params[0];

            // build the URL
            StringBuilder urlBuilder = new StringBuilder(getString(R.string.serverIP) + getString(R.string.apiReadOfferUrl));
            urlBuilder.append("?inquiry_id=");
            try {
                urlBuilder.append(URLEncoder.encode(inquiryId, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.d("ERROR", "Unsuported url encoding");
                return "exception";
            }
            try {
                // URL for login API endpoint
                url = new URL(urlBuilder.toString());
            } catch (MalformedURLException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

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
                    // Failed login attempt
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

            if(result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unauthorized")) {
                OfferActivity.this.checkOfferResponse.setTextColor(Color.RED);
                OfferActivity.this.checkOfferResponse.setText(R.string.offerFetchDataError);
                return;
            }

            // parse response json
            try {
                int offerId;

                jsonResponse = new JSONObject(result);
                offerId = Integer.parseInt((String)jsonResponse.get("offer_id"));
                OfferActivity.this.flightType.setText(OfferActivity.this.flightType.getText().toString() + (String)jsonResponse.get("flight_type"));
                OfferActivity.this.flightAccommodation.setText(OfferActivity.this.flightAccommodation.getText().toString() + (String)jsonResponse.get("flight_accommodation"));
                OfferActivity.this.ticketPrice.setText(OfferActivity.this.ticketPrice.getText().toString() + (String)jsonResponse.get("ticket_price"));
                OfferActivity.this.groundTranspPrice.setText(OfferActivity.this.groundTranspPrice.getText().toString() + (String)jsonResponse.get("ground_transp_price_per_person"));
                OfferActivity.this.hotelName.setText(OfferActivity.this.hotelName.getText().toString() + (String)jsonResponse.get("hotel_name"));
                OfferActivity.this.hotelAddress.setText(OfferActivity.this.hotelAddress.getText().toString() + (String)jsonResponse.get("hotel_address"));
                OfferActivity.this.numberOfHotelRooms.setText(OfferActivity.this.numberOfHotelRooms.getText().toString() + (String)jsonResponse.get("number_of_rooms"));
                OfferActivity.this.roomPricePerNight.setText(OfferActivity.this.roomPricePerNight.getText().toString() + (String)jsonResponse.get("room_price_per_night"));
                OfferActivity.this.tourGuide.setText(OfferActivity.this.tourGuide.getText().toString() + (String)jsonResponse.get("tour_guide"));
                OfferActivity.this.tourGuidePrice.setText(OfferActivity.this.tourGuidePrice.getText().toString() + (String)jsonResponse.get("price_tour_guide"));
                OfferActivity.this.activitiesDetails.setText(OfferActivity.this.activitiesDetails.getText().toString() + System.getProperty("line.separator") + (String)jsonResponse.get("activities_details"));
                OfferActivity.this.activitiesPricePerPerson.setText(OfferActivity.this.activitiesPricePerPerson.getText().toString() + (String)jsonResponse.get("activities_price_per_person"));
                OfferActivity.this.generalSuggestions.setText(OfferActivity.this.generalSuggestions.getText().toString() + System.getProperty("line.separator") + (String)jsonResponse.get("suggestions"));
                OfferActivity.this.totalPrice.setText(OfferActivity.this.totalPrice.getText().toString() + (String)jsonResponse.get("total_price"));

                // store offer id in outer class variable to access it when an action is taken
                OfferActivity.this.offerId = offerId;
            } catch (JSONException e) {
                // in case of invalid response from server
                Log.e("ERROR", e.getMessage(), e);
                OfferActivity.this.checkOfferResponse.setTextColor(Color.RED);
                OfferActivity.this.checkOfferResponse.setText(R.string.offerFetchDataError);
                return;
            }
        }
    }

    private class AsyncAcceptOffer extends AsyncTask<String, String, String>
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
            String offerId = params[1];

            // add data to json object
            try {
                jsonObj.put("jwt", jwt);
                jsonObj.put("offer_id", offerId);
            } catch(JSONException e) {
                // catch JSONException
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // URL for validate_token API endpoint
                url = new URL(getString(R.string.serverIP) + getString(R.string.apiAcceptOfferUrl));
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
                    // invalid JWT or unauthorized access attempt
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

            // failed
            if(result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unauthorized")) {
                OfferActivity.this.checkOfferResponse.setVisibility(View.VISIBLE);
                OfferActivity.this.checkOfferResponse.setTextColor(Color.RED);
                OfferActivity.this.checkOfferResponse.setText(R.string.offerAcceptError);
                return;
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);
                msg = (String)jsonResponse.get("message");

                if(!msg.equalsIgnoreCase("Offer Accepted")) {
                    // unknown error
                    OfferActivity.this.checkOfferResponse.setVisibility(View.VISIBLE);
                    OfferActivity.this.checkOfferResponse.setTextColor(Color.RED);
                    OfferActivity.this.checkOfferResponse.setText(R.string.offerAcceptError);
                } else {
                    // success
                    OfferActivity.this.checkOfferResponse.setVisibility(View.VISIBLE);
                    OfferActivity.this.checkOfferResponse.setTextColor(Color.BLACK);
                    OfferActivity.this.checkOfferResponse.setText(R.string.offerAccepted);

                    OfferActivity.this.offerAccept.setEnabled(false);
                    OfferActivity.this.offerDecline.setEnabled(false);
                }

            } catch (JSONException e) {
                // in case of invalid response from server (should not happen)
                Log.e("ERROR", e.getMessage(), e);
                Intent intent = new Intent(OfferActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }

    private class AsyncDeclineOffer extends AsyncTask<String, String, String>
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
            String offerId = params[1];
            String requestedChanged = params[2];

            // add data to json object
            try {
                jsonObj.put("jwt", jwt);
                jsonObj.put("offer_id", offerId);
                jsonObj.put("requested_changes", requestedChanged);
            } catch(JSONException e) {
                // catch JSONException
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // URL for validate_token API endpoint
                url = new URL(getString(R.string.serverIP) + getString(R.string.apiOfferRequestChangesUrl));
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
                    // invalid JWT, error, or unauthorized access attempt
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

            // failed
            if(result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception") || result.equalsIgnoreCase("unauthorized")) {
                OfferActivity.this.checkOfferResponse.setVisibility(View.VISIBLE);
                OfferActivity.this.checkOfferResponse.setTextColor(Color.RED);
                OfferActivity.this.checkOfferResponse.setText(R.string.offerRequestChangesError);
                return;
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);
                msg = (String)jsonResponse.get("message");

                if(!msg.equalsIgnoreCase("Changes Requested")) {
                    // unknown error
                    OfferActivity.this.checkOfferResponse.setVisibility(View.VISIBLE);
                    OfferActivity.this.checkOfferResponse.setTextColor(Color.RED);
                    OfferActivity.this.checkOfferResponse.setText(R.string.offerRequestChangesError);
                } else {
                    // success
                    OfferActivity.this.checkOfferResponse.setVisibility(View.VISIBLE);
                    OfferActivity.this.checkOfferResponse.setTextColor(Color.BLACK);
                    OfferActivity.this.checkOfferResponse.setText(R.string.offerChangesRequested);

                    OfferActivity.this.offerAccept.setEnabled(false);
                    OfferActivity.this.offerDecline.setEnabled(false);
                }
            } catch (JSONException e) {
                // in case of invalid response from server (should not happen)
                Log.e("ERROR", e.getMessage(), e);
                Intent intent = new Intent(OfferActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }
    }
}
