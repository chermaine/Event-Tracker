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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Chermaine on 8/10/17.
 */

public class SingleEventPage extends AppCompatActivity {
    private OkHttpClient okHttpClient;

    private TextView eventName;
    private TextView eventDate;
    private TextView eventTime;
    private TextView eventDescription;
    private Boolean allDay;

    private Bundle mBundle;
    private Bundle eventBundle;
    private Bundle userEventBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_page);

        mBundle = this.getIntent().getExtras();
        okHttpClient = new OkHttpClient();

        //get event details
        getEventDetails();

        //add event listener to buttons
        Button updateButton = (Button) findViewById(R.id.edit_event_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start edit event activity
                Intent intent = new Intent(SingleEventPage.this, UpdateEvent.class);

                //create a bundle with user infor and event infor
                userEventBundle = createUserEventBundle();
                intent.putExtras(userEventBundle);
                startActivity(intent);
            }
        });

        //add event listener to delete button
        Button deleteButton = (Button) findViewById(R.id.delete_event);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEvent();
            }
        });
    }

    protected void getEventDetails() {
        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/events/" + mBundle.getString("event_id"));

        //request
        Request request = new Request.Builder()
                .url(reqUrl)
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

                try {
                    final JSONObject jsonObject = new JSONObject(r);

                    //check if any error
                    if (jsonObject.has("error")) {
                        //report error
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

                    //no error display event details
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    eventName = (TextView) findViewById(R.id.event_name);
                                    eventName.setText(jsonObject.getString("name"));

                                    eventDate = (TextView) findViewById(R.id.event_date);
                                    eventDate.setText(jsonObject.getString("date"));

                                    eventTime = (TextView) findViewById(R.id.event_time);
                                    //check if all day is true
                                    if (jsonObject.getBoolean("all_day")) {
                                        allDay = true;
                                        eventTime.setText("All Day");
                                    }
                                    else {
                                        allDay = false;
                                        eventTime.setText(jsonObject.getString("time"));
                                    }

                                    eventDescription = (TextView) findViewById(R.id.event_description);
                                    eventDescription.setText(jsonObject.getString("description"));
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        //create event bundle
                        eventBundle = createEventBundle(jsonObject);
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected Bundle createEventBundle(JSONObject jsonObject) {
        Bundle newB = new Bundle();
        try {
            newB.putString("name", jsonObject.getString("name"));
            newB.putString("date", jsonObject.getString("date"));
            newB.putString("time", jsonObject.getString("time"));
            newB.putString("description", jsonObject.getString("description"));
            newB.putBoolean("all_day", jsonObject.getBoolean("all_day"));
            newB.putString("id", jsonObject.getString("id"));
            newB.putString("account_id", jsonObject.getString("account_id"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        newB.putBoolean("fb_login", mBundle.getBoolean("fb_login"));
        return newB;
    }

    protected Bundle createUserEventBundle() {
        Bundle newB = new Bundle();

        //copy all value in mBundle to newB
        newB.putString("first_name", mBundle.getString("first_name"));
        newB.putString("last_name", mBundle.getString("last_name"));
        newB.putString("username", mBundle.getString("username"));
        newB.putString("email", mBundle.getString("email"));
        newB.putString("id", mBundle.getString("id"));
        newB.putString("events", mBundle.getString("events"));
        newB.putString("password", mBundle.getString("password"));
        newB.putBoolean("fb_login", mBundle.getBoolean("fb_login"));

        //copy all value in eventBundle to newB
        newB.putString("name", eventBundle.getString("name"));
        newB.putString("date", eventBundle.getString("date"));
        newB.putString("time", eventBundle.getString("time"));
        newB.putString("description", eventBundle.getString("description"));
        newB.putBoolean("all_day", eventBundle.getBoolean("all_day"));
        newB.putString("event_id", eventBundle.getString("id"));

        //return
        return newB;
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
                //direct to add new event activity
                Intent intent = new Intent(SingleEventPage.this, AddNewEventActivity.class);
                intent.putExtras(mBundle);
                startActivity(intent);
                return true;

            case R.id.action_user_profile:
                //direct to user profile activity
                Intent userProfile = new Intent(SingleEventPage.this, UserProfileActivity.class);
                userProfile.putExtras(mBundle);
                startActivity(userProfile);
                return true;

            case R.id.fb_action_show_past_event:
            case R.id.action_show_past_event:
                //direct to user past event activity
                Intent history = new Intent(SingleEventPage.this, ShowPastEventActivity.class);
                history.putExtras(mBundle);
                startActivity(history);
                return true;

            case R.id.action_logout:
                //logout and direct to main activity
                Intent main = new Intent(SingleEventPage.this, MainActivity.class);
                startActivity(main);
                return true;

            case R.id.fb_action_logout:
                LoginManager.getInstance().logOut();
                Intent main2 = new Intent(SingleEventPage.this, MainActivity.class);
                startActivity(main2);
                return true;

            case R.id.action_home:
                Intent home = new Intent(SingleEventPage.this, HomePageActivity.class);
                home.putExtras(mBundle);
                startActivity(home);
                return true;

            case R.id.fb_action_home:
                Intent fbHome = new Intent(SingleEventPage.this, FBHomePage.class);
                fbHome.putExtras(mBundle);
                startActivity(fbHome);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void deleteEvent() {
        Log.i("deleteEvent():", String.format("eventID = %s", mBundle.getString("event_id")));

        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/events/"
                + mBundle.getString("event_id"));

        //set request
        Request request = new Request.Builder()
                .url(reqUrl)
                .delete()
                .build();

        //send request
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("response", response.body().toString());

                String r = response.body().string();
                Log.i("deleteResponse", r);

                //Log.i("deleteResponse:", String.format("deleteResponse: %s", r));

                if (r != null && r.length() > 0) {
                    try {
                        final JSONObject resp = new JSONObject(r);

                        //check if error
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
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //no error
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Deleted event", Toast.LENGTH_LONG).show();
                        }
                    });

                    //remove event_id from mBundle
                    mBundle.remove("event_id");

                    //direct user back to appropriate home page
                    if (mBundle.getBoolean("fb_login")) {
                        Intent fbIntent = new Intent (SingleEventPage.this, FBHomePage.class);
                        fbIntent.putExtras(mBundle);
                        startActivity(fbIntent);
                    }
                    else {
                        Intent intent = new Intent(SingleEventPage.this, HomePageActivity.class);
                        intent.putExtras(mBundle);
                        startActivity(intent);
                    }
                }
            }
        });

    }
}
