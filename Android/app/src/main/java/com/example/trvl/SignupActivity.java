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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class SignupActivity extends AppCompatActivity {

    private EditText firstName;
    private EditText lastName;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private EditText phone;
    private EditText dateOfBirth;
    private Spinner gender;
    private Spinner country;
    private EditText city;
    private EditText address;
    private EditText nameOnCard;
    private EditText cardNumber;
    private EditText expirationDate;
    private EditText cvv;

    private TextView signupResponse;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        this.firstName = (EditText)findViewById(R.id.firstName);
        this.lastName = (EditText)findViewById(R.id.lastName);
        this.email = (EditText)findViewById(R.id.email);
        this.password = (EditText)findViewById(R.id.password);
        this.confirmPassword = (EditText)findViewById(R.id.confirmPassword);
        this.phone = (EditText)findViewById(R.id.phone);
        this.dateOfBirth = (EditText)findViewById(R.id.dateOfBirth);
        this.city = (EditText)findViewById(R.id.city);
        this.address = (EditText)findViewById(R.id.address);
        this.nameOnCard = (EditText)findViewById(R.id.nameOnCard);
        this.cardNumber = (EditText)findViewById(R.id.cardNumber);
        this.expirationDate = (EditText)findViewById(R.id.expirationDate);
        this.cvv = (EditText)findViewById(R.id.cvv);
        this.signupResponse = (TextView)findViewById(R.id.signUpResponse);
        this.login = (Button)findViewById(R.id.signUpLoginbtn);

        // init spinners
        this.gender = (Spinner)findViewById(R.id.gender);
        this.country =  (Spinner)findViewById(R.id.country);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> gAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        gAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        gender.setAdapter(gAdapter);

        // init spinner country
        ArrayAdapter<CharSequence> cAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries, android.R.layout.simple_spinner_item);
        cAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(cAdapter);

        // set values to default
        gender.setSelection(0);
        country.setSelection(0);

    }

    public void goToLoginActivity(View view) {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.putExtra("nextActivity", "AccountHome");
        startActivity(intent);
    }

    public void signup(View view) {
        if(
            this.firstName.getText().toString().equals("") ||
            this.lastName.getText().toString().equals("") ||
            this.email.getText().toString().equals("") ||
            this.password.getText().toString().equals("") ||
            this.confirmPassword.getText().toString().equals("") ||
            this.phone.getText().toString().equals("") ||
            this.dateOfBirth.getText().toString().equals("") ||
            this.city.getText().toString().equals("") ||
            this.address.getText().toString().equals("") ||
            this.nameOnCard.getText().toString().equals("") ||
            this.cardNumber.getText().toString().equals("") ||
            this.expirationDate.getText().toString().equals("") ||
            this.cvv.getText().toString().equals("") ||
            this.gender.getSelectedItem().toString().equalsIgnoreCase("Gender") ||
            this.country.getSelectedItem().toString().equalsIgnoreCase("Country")
        ) {
            this.signupResponse.setTextColor(Color.RED);
            this.signupResponse.setText(R.string.signUpRespAllFieldsRequired);
            return;
        }

        if(!this.password.getText().toString().equals(this.confirmPassword.getText().toString())) {
            this.signupResponse.setTextColor(Color.RED);
            this.signupResponse.setText(R.string.signupRespPassNoMatch);
            return;
        }


        // launch background thread to communicate to server
        new AsyncSignup().execute(
                this.firstName.getText().toString(),
                this.lastName.getText().toString(),
                this.email.getText().toString(),
                this.password.getText().toString(),
                this.confirmPassword.getText().toString(),
                this.phone.getText().toString(),
                this.dateOfBirth.getText().toString(),
                this.gender.getSelectedItem().toString(),
                this.country.getSelectedItem().toString(),
                this.city.getText().toString(),
                this.address.getText().toString(),
                this.nameOnCard.getText().toString(),
                this.cardNumber.getText().toString(),
                this.expirationDate.getText().toString(),
                this.cvv.getText().toString()
        );
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncSignup extends AsyncTask<String, String, String> {
        public static final int CONNECTION_TIMEOUT=10000;
        public static final int READ_TIMEOUT=15000;

        ProgressDialog pdLoading = new ProgressDialog(SignupActivity.this);
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
            String firstName = params[0];
            String lastName = params[1];
            String email = params[2];
            String password = params[3];
            String confirmPassword = params[4];
            String phone = params[5];
            String dateOfBirth = params[6];
            String gender = params[7];
            String country = params[8];
            String city = params[9];
            String address = params[10];
            String nameOnCard = params[11];
            String cardNumber = params[12];
            String expirationDate = params[13];
            String cvv = params[14];

            // add data to json object
            try {
                jsonObj.put("firstname", firstName);
                jsonObj.put("lastname", lastName);
                jsonObj.put("email", email);
                jsonObj.put("password", password);
                jsonObj.put("confirm_password", confirmPassword);
                jsonObj.put("phone", phone);
                jsonObj.put("date_of_birth", dateOfBirth);
                jsonObj.put("gender", gender);
                jsonObj.put("country", country);
                jsonObj.put("city", city);
                jsonObj.put("address", address);
                jsonObj.put("name_on_card", nameOnCard);
                jsonObj.put("card_number", cardNumber);
                jsonObj.put("expiration_date", expirationDate);
                jsonObj.put("cvv", cvv);

            } catch (JSONException e) {
                // catch JSONException
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // URL for login API endpoint
                url = new URL(getString(R.string.serverIP) + getString(R.string.apiSignupUrl));

            } catch (MalformedURLException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(AsyncSignup.READ_TIMEOUT);
                conn.setConnectTimeout(AsyncSignup.CONNECTION_TIMEOUT);
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

                } else if(response_code == HttpURLConnection.HTTP_BAD_REQUEST) {
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

            // stop loading dialog
            pdLoading.dismiss();

            // to hold the json data returned by the server
            JSONObject jsonResponse;

            // notify user that an unknown error occured
            if (result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception")) {
                SignupActivity.this.signupResponse.setTextColor(Color.RED);
                SignupActivity.this.signupResponse.setText(R.string.signupRespError);
                return;
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);

                if(jsonResponse.get("error").toString().equalsIgnoreCase("")) {
                    // success
                    SignupActivity.this.signupResponse.setTextColor(Color.BLACK);
                    SignupActivity.this.signupResponse.setText(R.string.signupRespSuccess);
                    SignupActivity.this.login.setVisibility(View.VISIBLE);
                } else {
                    // display error
                    SignupActivity.this.signupResponse.setTextColor(Color.RED);
                    SignupActivity.this.signupResponse.setText(jsonResponse.get("error").toString());
                }
            } catch (JSONException e) {
                // in case of invalid response from server
                Log.e("ERROR", e.getMessage(), e);
                SignupActivity.this.signupResponse.setText(R.string.loginRespError);
            }
        }
    }
}