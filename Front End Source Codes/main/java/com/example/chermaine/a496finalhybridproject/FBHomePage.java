package com.example.chermaine.a496finalhybridproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Created by Chermaine on 8/12/17.
 */

public class FBHomePage extends AppCompatActivity {
    private OkHttpClient mOkHttpClient;
    private Bundle mBundle;
    private ArrayList<String> eventIDs;
    private AccessToken accessToken;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mOkHttpClient = new OkHttpClient();

        //receive date from getExtras
        mBundle = this.getIntent().getExtras();

        //get user's first_name from bundle
        TextView firstName = (TextView) findViewById(R.id.first_name);

        //set greetings with first name
        firstName.setText(mBundle.getString("first_name"));

        //get not you button
        Button notYou = (Button) findViewById(R.id.not_you_button);
        notYou.setText(String.format("Not you, %s? Logout", mBundle.getString("first_name")));
        notYou.setVisibility(View.VISIBLE);
        notYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(FBHomePage.this, MainActivity.class);
                startActivity(intent);
            }
        });

        fbGetEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fbmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fb_action_add_new_event:
                //direct to add new event activity
                Intent intent = new Intent(FBHomePage.this, AddNewEventActivity.class);
                intent.putExtras(mBundle);
                startActivity(intent);
                return true;

            case R.id.fb_action_show_past_event:
                //direct to user past event activity
                Intent history = new Intent(FBHomePage.this, ShowPastEventActivity.class);
                history.putExtras(mBundle);
                startActivity(history);
                return true;

            case R.id.fb_action_logout:
                //logout and direct to main activity
                LoginManager.getInstance().logOut();
                Intent main = new Intent(FBHomePage.this, MainActivity.class);
                startActivity(main);
                return true;

            case R.id.fb_action_home:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //get events associated with user's facebook account
    //send GET request to /events/users/{{user id}}
    protected void fbGetEvents() {
        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/events/users/"
                + mBundle.getString("id")
        );

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
                //get response from response body
                String r = response.body().string();
                Log.i("onResponse:", String.format("onResponse FB home page: r = %s", r));

                if (r != null && r.length() > 0) {
                    try {
                        //create json object for response body
                        JSONObject jsonObject = new JSONObject(r);
                        JSONArray jsonArray = jsonObject.getJSONArray("events");
                        populateEvents(jsonArray);
                    }
                    catch (JSONException e){
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
            eventIDs = new ArrayList<String>();

            //get details of each event and build hashMap for each event
            for (int i = 0; i < jsonArray.length(); i++) {
                HashMap<String, String> e = new HashMap<String, String>();
                e.put("name", jsonArray.getJSONObject(i).getString("name"));
                e.put("date", jsonArray.getJSONObject(i).getString("date"));

                //if all_day is true, set time to All Day
                if (jsonArray.getJSONObject(i).getBoolean("all_day")) {
                    e.put("time", "All Day");
                }
                else {
                    e.put("time", jsonArray.getJSONObject(i).getString("time"));
                }

                e.put("description", jsonArray.getJSONObject(i).getString("description"));
                e.put("id", jsonArray.getJSONObject(i).getString("id"));
                eventIDs.add(jsonArray.getJSONObject(i).getString("id"));
                events.add(e);
            }

            //get ListView from layout
            final ListView mList = (ListView) findViewById(R.id.list_view);

            //setup columns for list view
            String[] columnNames = {
                    "name",
                    "date",
                    "time",
                    "description",
                    "id"
            };

            //setup layout id for list view
            int[] elementIds = {
                    R.id.event_name,
                    R.id.event_date,
                    R.id.event_time,
                    R.id.event_description,
                    R.id.event_id
            };

            //create a simple adapter to display each event
            final SimpleAdapter mSimpleAdapter = new SimpleAdapter(
                    FBHomePage.this,
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

            Log.i("eventIds: ", String.format("eventIDS = %s", eventIDs.toString()));
            mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //an event is clicked, get the event and display details and options
                    Intent intent = new Intent(FBHomePage.this, SingleEventPage.class);

                    //put event id into bundle
                    mBundle.putString("event_id", eventIDs.get(i));
                    intent.putExtras(mBundle);
                    startActivity(intent);
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

