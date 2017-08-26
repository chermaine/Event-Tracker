package com.example.chermaine.a496finalhybridproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class UpdateUserProfile extends AppCompatActivity {
    private Bundle mBundle;
    private OkHttpClient okHttpClient;

    private EditText firstName;
    private EditText lastName;
    private EditText username;
    private EditText email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_user_profile);

        //get user info from bundle
        mBundle = this.getIntent().getExtras();

        Log.i("UpdateUser:", String.format("UpdateUserProfile onCreate userID = %s", mBundle.getString("id")));

        //set EditTexts hint
        firstName = (EditText) findViewById(R.id.update_first_name);
        firstName.setHint(mBundle.getString("first_name"));

        lastName = (EditText) findViewById(R.id.update_last_name);
        lastName.setHint(mBundle.getString("last_name"));

        username = (EditText) findViewById(R.id.update_username);
        username.setHint(mBundle.getString("username"));

        email = (EditText) findViewById(R.id.update_email);
        email.setHint(mBundle.getString("email"));

        //add event listener to submit button
        Button submit = (Button) findViewById(R.id.update_profile_submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update profile
                updateProfile();
            }
        });

        //add event listener to cancel button
        Button cancel = (Button) findViewById(R.id.update_profile_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //direct user back to user profile activity
                Intent intent = new Intent(UpdateUserProfile.this, UserProfileActivity.class);
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        });
    }

    protected void updateProfile() {
        //create http client
        okHttpClient = new OkHttpClient();

        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/users/" + mBundle.getString("id"));

        //set media type
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        //set json request body
        JSONObject body = new JSONObject();
        try {
            //check for new first name
            if (firstName.getText() != null && firstName.getText().length() > 0) {
                body.put("first_name", firstName.getText());
            }

            //check for new last name
            if (lastName.getText() != null && lastName.getText().length() > 0) {
                body.put("last_name", lastName.getText());
            }

            //check for new username
            if (username.getText() != null && username.getText().length() > 0) {
                body.put("username", username.getText());
            }

            //check for new email
            if (email.getText() != null && email.getText().length() > 0) {
                body.put("email", email.getText());
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        //check if body is empty
        if (body.length() == 0) {
            Toast.makeText(getApplicationContext(), "Error: At least one input field is required", Toast.LENGTH_LONG).show();
        }
        else {
            //set request body
            RequestBody requestBody = RequestBody.create(mediaType, body.toString());

            //set request
            Request request = new Request.Builder()
                    .url(reqUrl)
                    .put(requestBody)
                    .build();

            //send request
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //get response
                    String r = response.body().string();
                    Log.i("response: ", String.format("onResponse(): %s", r));

                    try {
                        final JSONObject jsonObject = new JSONObject(r);

                        //check if any error
                        if (jsonObject.has("error")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Toast.makeText(getApplicationContext(), String.format("Error: %s", jsonObject.getString("error")), Toast.LENGTH_LONG).show();
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                        //no error, direct to user profile activity with updated bundle
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Updated user profile", Toast.LENGTH_LONG).show();
                                }
                            });

                            //create new bundle
                            Bundle newBundle = new Bundle();

                            newBundle.putString("id", jsonObject.getString("id"));
                            newBundle.putString("first_name", jsonObject.getString("first_name"));
                            newBundle.putString("last_name", jsonObject.getString("last_name"));
                            newBundle.putString("username", jsonObject.getString("username"));
                            newBundle.putString("email", jsonObject.getString("email"));
                            newBundle.putString("events", jsonObject.getString("events"));
                            newBundle.putString("password", jsonObject.getString("password"));

                            //set intent
                            Intent intent = new Intent(UpdateUserProfile.this, UserProfileActivity.class);
                            intent.putExtras(newBundle);
                            startActivity(intent);
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
