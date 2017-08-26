package com.example.chermaine.a496finalhybridproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Chermaine on 8/9/17.
 */

public class SignupActivity extends AppCompatActivity {
    private EditText firstName;
    private EditText lastName;
    private EditText username;
    private EditText email;
    private EditText password;
    private CheckBox showPassword;

    private OkHttpClient mOkHttpClient;
    private static final String API_KEY = "AIzaSyCeMF8ZsFRypkqXVfit96pmBgnICGVH-XQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //get password EditText
        password = (EditText) findViewById(R.id.input_password);

        //get show password checkbox
        showPassword = (CheckBox) findViewById(R.id.show_password);

        //add on checkedlistener for checkbox
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //show password
                if (!isChecked) {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                //hide password
                else {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
        Button submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewAccount();
            }
        });

    }

    protected void addNewAccount() {
        //get all inputs
        firstName = (EditText) findViewById(R.id.input_first_name);
        lastName = (EditText) findViewById(R.id.input_last_name);
        username = (EditText) findViewById(R.id.input_username);
        email = (EditText) findViewById(R.id.input_email);
        password = (EditText) findViewById(R.id.input_password);

        //setup json object for request body
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("first_name", firstName.getText());
            jsonData.put("last_name", lastName.getText());
            jsonData.put("username", username.getText());
            jsonData.put("email", email.getText());
            jsonData.put("password", password.getText());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        //send POST request to backend to create a new account
        try {
            //create new Http client
            mOkHttpClient = new OkHttpClient();

            //set request url
            HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/users");

            //set request body
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(mediaType, jsonData.toString());

            //create request with url and request body
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .post(body)
                    .build();

            //send request
            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //get response body
                    String r = response.body().string();
                    Log.i("onResponse: ", String.format("New sign up callback return: %s", r));

                    //create a bundle to pass to next activity
                    Bundle bundle = new Bundle();

                    //add response body to bundle
                    try {
                        final JSONObject jsonData = new JSONObject(r);

                        if (jsonData.has("error")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Toast.makeText(getApplicationContext(), String.format("Error: %s", jsonData.getString("error")), Toast.LENGTH_LONG).show();
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        else {
                            if (jsonData.has("first_name")) {
                                bundle.putString("first_name", jsonData.getString("first_name"));
                            }
                            if (jsonData.has("last_name")) {
                                bundle.putString("last_name", jsonData.getString("last_name"));
                            }
                            if (jsonData.has("username")) {
                                bundle.putString("username", jsonData.getString("username"));
                            }
                            if (jsonData.has("email")) {
                                bundle.putString("email", jsonData.getString("email"));
                            }
                            if (jsonData.has("password")){
                                bundle.putString("password", jsonData.getString("password"));
                            }
                            if (jsonData.has("events")) {
                                bundle.putString("events", jsonData.getString("events"));
                            }
                            if (jsonData.has("id")) {
                                bundle.putString("id", jsonData.getString("id"));
                            }

                            //start new activity with bundle
                            Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
