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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Chermaine on 8/10/17.
 */

public class ShowPastEventActivity extends AppCompatActivity {
    private OkHttpClient mOkHttpClient;
    private Bundle mBundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //receive date from getExtras
        mBundle = this.getIntent().getExtras();

        //create OkHttpClient
        mOkHttpClient = new OkHttpClient();

        //get list of past events associated with user account
        if (mBundle.getBoolean("fb_login")) {
            getFBPastEvents();
        }
        else {
            getPastEvents();
        }

        //add event listener to clear history button
        Button clearHistory = (Button) findViewById(R.id.clear_history_button);
        clearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearUserEventHistory();
            }
        });
    }

    //send GET request to /history/{{user id}} to get list of past events of this user
    //different path because users with Facebook account did not create a new account using API
    protected void getFBPastEvents() {
        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/history/" + mBundle.getString("id"));

        //set request
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();

        //send request
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();
                //if r is not empty, get response and populate events
                if (r != null && r.length() > 0) {
                    try {
                        JSONObject jsonObject = new JSONObject(r);
                        JSONArray jsonArray = jsonObject.getJSONArray("history");
                        populateEvents(jsonArray);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //send GET request to /users/{{user id}}/events/history
    //get past events for user with an account in datastore
    protected void getPastEvents() {
        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/users/" +
                mBundle.getString("id")
                + "/events/history");

        //set request
        Request request = new Request.Builder()
                .url(reqUrl)
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

                if (r != null && r.length() > 0) {
                    try {
                        //create json object for response body
                        JSONObject jsonObject = new JSONObject(r);
                        JSONArray jsonArray = jsonObject.getJSONArray("history");

                        populateEvents(jsonArray);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    protected void populateEvents(JSONArray jsonArray) {
        try {
            //create a List of HashMap for each post
            List<Map<String, String>> events = new ArrayList<Map<String, String>>();

            //get details of each event and build hashMap for each event
            for (int i = 0; i < jsonArray.length(); i++) {
                HashMap<String, String> e = new HashMap<String, String>();
                e.put("name", jsonArray.getJSONObject(i).getString("name"));
                e.put("date", jsonArray.getJSONObject(i).getString("date"));

                //if all_day is true, set time to All Day
                if (jsonArray.getJSONObject(i).getBoolean("all_day")) {
                    e.put("time", "All Day");
                } else {
                    e.put("time", jsonArray.getJSONObject(i).getString("time"));
                }

                e.put("description", jsonArray.getJSONObject(i).getString("description"));
                events.add(e);
            }

            //get ListView from layout
            final ListView mList = (ListView) findViewById(R.id.list_view);

            //setup columns for list view
            String[] columnNames = {
                    "name",
                    "date",
                    "time",
                    "description"
            };

            //setup layout id for list view
            int[] elementIds = {
                    R.id.event_name,
                    R.id.event_date,
                    R.id.event_time,
                    R.id.event_description
            };

            //create a simple adapter to display each post
            final SimpleAdapter mSimpleAdapter = new SimpleAdapter(
                    ShowPastEventActivity.this,
                    events,
                    R.layout.event_details,
                    columnNames,
                    elementIds
            );

            //display events
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mList.setAdapter(mSimpleAdapter);
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
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
                Intent history = new Intent(ShowPastEventActivity.this, AddNewEventActivity.class);
                history.putExtras(mBundle);
                startActivity(history);
                return true;

            case R.id.action_user_profile:
                //direct to user profile activity
                Intent userProfile = new Intent(ShowPastEventActivity.this, UserProfileActivity.class);
                userProfile.putExtras(mBundle);
                startActivity(userProfile);
                return true;

            case R.id.fb_action_show_past_event:
            case R.id.action_show_past_event:
                //direct to user past event activity
                return true;

            case R.id.action_logout:
                //logout and direct to main activity
                Intent main = new Intent(ShowPastEventActivity.this, MainActivity.class);
                startActivity(main);
                return true;

            case R.id.fb_action_logout:
                LoginManager.getInstance().logOut();
                Intent main2 = new Intent(ShowPastEventActivity.this, MainActivity.class);
                startActivity(main2);
                return true;

            case R.id.action_home:
                //direct user to home page
                Intent home = new Intent(ShowPastEventActivity.this, HomePageActivity.class);
                home.putExtras(mBundle);
                startActivity(home);
                return true;

            case R.id.fb_action_home:
                Intent fbHome = new Intent(ShowPastEventActivity.this, FBHomePage.class);
                fbHome.putExtras(mBundle);
                startActivity(fbHome);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void clearUserEventHistory() {
        HttpUrl reqUrl;

        if (mBundle.getBoolean("fb_login")) {
            reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/history/" + mBundle.getString("id"));
        }
        else {
            reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/users/"
                    + mBundle.getString("id")
                    + "/events/history");
        }

        //set request
        Request request = new Request.Builder()
                .url(reqUrl)
                .delete()
                .build();

        //send request
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();

                if (r != null && r.length() > 0) {
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
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Cleared history", Toast.LENGTH_LONG).show();
                        }
                    });

                    Intent intent = new Intent(ShowPastEventActivity.this, ShowPastEventActivity.class);
                    intent.putExtras(mBundle);
                    startActivity(intent);
                }
            }
        });
    }
}
