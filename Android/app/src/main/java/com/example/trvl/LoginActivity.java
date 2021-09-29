package com.example.trvl;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private static String nextActivity;

    private EditText email;
    private EditText password;
    private TextView response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        LoginActivity.nextActivity = intent.getStringExtra("nextActivity");

        this.email = (EditText)findViewById(R.id.inputEmail);
        this.password = (EditText)findViewById(R.id.inputPassword);
        this.response = (TextView)findViewById(R.id.textViewResponse);
    }

    @SuppressLint("SetTextI18n")
    public void login(View view) {

        String email;
        String password;

        // if empty email or password
        if (LoginActivity.this.email.getText().toString().equals("") || LoginActivity.this.password.toString().equals("")) {
            response.setText(R.string.emptyEmailOrPassword);
            return;
        }
        email = LoginActivity.this.email.getText().toString();
        password = LoginActivity.this.password.getText().toString();

        // launch background thread to communicate to server
        new AsyncLogin().execute(email, password);
    }


    private class AsyncLogin extends AsyncTask<String, String, String>
    {
        public static final int CONNECTION_TIMEOUT=10000;
        public static final int READ_TIMEOUT=15000;

        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {

            JSONObject jsonObj = new JSONObject();
            String jsonData = "";

            // fields data
            String email = params[0];
            String password = params[1];

            // add data to json object
            try {
                jsonObj.put("email", email);
                jsonObj.put("password", password);
            } catch(JSONException e) {
                // catch JSONException
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }

            try {
                // URL for login API endpoint
                url = new URL(getString(R.string.serverIP) + getString(R.string.apiLoginUrl));

            } catch (MalformedURLException e) {
                Log.e("ERROR", e.getMessage(), e);
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(AsyncLogin.READ_TIMEOUT);
                conn.setConnectTimeout(AsyncLogin.CONNECTION_TIMEOUT);
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

        @Override
        protected void onPostExecute(String result) {
            pdLoading.dismiss();

            JSONObject jsonResponse;
            String JWT = "";

            if(result.equalsIgnoreCase("error") || result.equalsIgnoreCase("exception")) {
                LoginActivity.this.response.setText(R.string.loginRespError);
                return;
            } else if(result.equalsIgnoreCase("unauthorized")) {
                LoginActivity.this.response.setText(R.string.loginRespFailed);
                return;
            } else {
                LoginActivity.this.response.setText("Login successful");
            }

            // parse response json
            try {
                jsonResponse = new JSONObject(result);
                Log.d("JWT", (String) jsonResponse.get("jwt"));
                // get JWT from response
                JWT = (String)jsonResponse.get("jwt");
            } catch (JSONException e) {
                // in case of invalid response from server
                Log.e("ERROR", e.getMessage(), e);
                LoginActivity.this.response.setText(R.string.loginRespError);
                return;
            }

            // store jwt
            // must be destroyed on logout
            SharedPreferences pref = getApplicationContext().getSharedPreferences(getString(R.string.prefJWTFile), Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = pref.edit();
            editor.putString(getString(R.string.savedJWT), JWT);
            editor.apply();

            if(LoginActivity.nextActivity.equalsIgnoreCase("InquireActivity")) {
                Intent intent = new Intent(LoginActivity.this, InquireActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(LoginActivity.this, AccountHomeActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    public void signup(View view) {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        LoginActivity.this.startActivity(intent);
        finish();
    }
}