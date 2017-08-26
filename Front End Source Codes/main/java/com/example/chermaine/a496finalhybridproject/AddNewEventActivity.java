package com.example.chermaine.a496finalhybridproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginManager;

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

public class AddNewEventActivity extends AppCompatActivity {
    private Bundle mBundle;
    private OkHttpClient mOkHttpClient;

    private EditText eventName;
    private EditText eventDate;
    private EditText eventTime;
    private EditText eventDescription;
    private CheckBox eventAllDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);

        //get user infor from bundle
        mBundle = this.getIntent().getExtras();

        //add event listener to submit button
        Button submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add new event
                addNewEvent();
            }
        });

        //add event listener to cancel button
        Button cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (mBundle.getBoolean("fb_login")) {
                    intent = new Intent(AddNewEventActivity.this, FBHomePage.class);
                }
                else {
                    intent = new Intent(AddNewEventActivity.this, HomePageActivity.class);
                }
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        });
    }

    protected void addNewEvent() {
        //get event information from input
        eventName = (EditText) findViewById(R.id.input_event_name);
        eventDate = (EditText) findViewById(R.id.input_event_date);
        eventTime = (EditText) findViewById(R.id.input_event_time);
        eventDescription = (EditText) findViewById(R.id.input_event_description);
        eventAllDay = (CheckBox) findViewById(R.id.input_event_all_day);

        //create new http client
        mOkHttpClient = new OkHttpClient();

        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/events");

        //set media type
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        //set request body
        JSONObject jsonObject = new JSONObject();
        try {
            if (eventName.getText() != null && eventName.getText().length() > 0) {
                jsonObject.put("name", eventName.getText());
            }
            if (eventDate.getText() != null && eventDate.getText().length() > 0) {
                jsonObject.put("date", eventDate.getText());
            }
            if (eventTime.getText() != null && eventTime.getText().length() > 0) {
                jsonObject.put("time", eventTime.getText());
            }
            if (eventDescription.getText() != null && eventDescription.getText().length() > 0) {
                jsonObject.put("description", eventDescription.getText());
            }
            jsonObject.put("all_day", eventAllDay.isChecked());
            jsonObject.put("account_id", mBundle.getString("id"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        //set request
        Request request = new Request.Builder()
                .url(reqUrl)
                .post(requestBody)
                .build();

        //send request
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //get response
                String r = response.body().string();

                Log.i("onResponse", String.format("On response: r = %s", r));
                try {
                    final JSONObject resp = new JSONObject(r);
                    if (resp.has("error")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(getApplicationContext(), String.format("Error: %s", resp.getString("error")), Toast.LENGTH_LONG).show();
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else {
                        //check if user logged in using FB
                        if (mBundle.getBoolean("fb_login")) {
                            Intent fbIntent = new Intent(AddNewEventActivity.this, FBHomePage.class);
                            fbIntent.putExtras(mBundle);
                            startActivity(fbIntent);
                        }
                        else {
                            Intent intent = new Intent(AddNewEventActivity.this, HomePageActivity.class);
                            intent.putExtras(mBundle);
                            startActivity(intent);
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (mBundle.getBoolean("fb_login")) {
            inflater.inflate(R.menu.fbmenu, menu);
        }
        else {
            inflater.inflate(R.menu.menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fb_action_add_new_event:
            case R.id.action_add_new_event:
                return true;

            case R.id.action_user_profile:
                //direct to user profile activity
                Intent userProfile = new Intent(AddNewEventActivity.this, UserProfileActivity.class);
                userProfile.putExtras(mBundle);
                startActivity(userProfile);
                return true;

            case R.id.fb_action_show_past_event:
            case R.id.action_show_past_event:
                //direct to user past event activity
                Intent history = new Intent(AddNewEventActivity.this, ShowPastEventActivity.class);
                history.putExtras(mBundle);
                startActivity(history);
                return true;

            case R.id.action_logout:
                //logout and direct to main activity
                Intent main = new Intent(AddNewEventActivity.this, MainActivity.class);
                startActivity(main);
                return true;

            case R.id.fb_action_logout:
                LoginManager.getInstance().logOut();
                Intent main2 = new Intent(AddNewEventActivity.this, MainActivity.class);
                startActivity(main2);
                return true;

            case R.id.action_home:
                //direct user to home page
                Intent home = new Intent(AddNewEventActivity.this, HomePageActivity.class);
                home.putExtras(mBundle);
                startActivity(home);
                return true;

            case R.id.fb_action_home:
                Intent fbHome = new Intent(AddNewEventActivity.this, FBHomePage.class);
                fbHome.putExtras(mBundle);
                startActivity(fbHome);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
