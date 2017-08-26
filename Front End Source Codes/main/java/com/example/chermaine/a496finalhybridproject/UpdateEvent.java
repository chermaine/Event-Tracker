package com.example.chermaine.a496finalhybridproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
 * Created by Chermaine on 8/10/17.
 */

public class UpdateEvent extends AppCompatActivity {
    private EditText eventName;
    private EditText date;
    private EditText time;
    private EditText description;
    private CheckBox allDay;
    private Boolean curAllDay;

    private Bundle mBundle;
    private Bundle newBundle;

    private OkHttpClient okHttpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_event);

        //get infor from bundle
        mBundle = this.getIntent().getExtras();

        Log.i("UpdateEvent:", String.format("UpdateEvent onCreate eventID = %s, userID = %s", mBundle.getString("event_id"), mBundle.getString("id")));

        //set EditTexts hint
        eventName = (EditText) findViewById(R.id.update_event_name);
        eventName.setHint(mBundle.getString("name"));

        date = (EditText) findViewById(R.id.update_event_date);
        date.setHint(mBundle.getString("date"));

        time = (EditText) findViewById(R.id.update_event_time);
        time.setHint(mBundle.getString("time"));

        description = (EditText) findViewById(R.id.update_event_description);
        description.setHint(mBundle.getString("description"));

        //check or uncheck all day
        allDay = (CheckBox) findViewById(R.id.update_all_day);
        if (mBundle.getBoolean("all_day")) {
            allDay.setChecked(true);
            curAllDay = true;
        }
        else {
            curAllDay = false;
        }

        //add event listener to submit button
        Button submit = (Button) findViewById(R.id.update_event_submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateEventDetail();
            }
        });

        //add event listener to cancel button
        Button cancel = (Button) findViewById(R.id.update_event_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //direct user back to event detail page
                Intent intent = new Intent(UpdateEvent.this, SingleEventPage.class);

                //remove some values in bundle
                mBundle.remove("name");
                mBundle.remove("date");
                mBundle.remove("time");
                mBundle.remove("description");
                mBundle.remove("all_day");

                intent.putExtras(mBundle);
                startActivity(intent);
            }
        });
    }

    protected void updateEventDetail() {
        okHttpClient = new OkHttpClient();

        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/events/" + mBundle.getString("event_id"));

        //set media type
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        //set request body
        final JSONObject jsonObject = new JSONObject();
        try {
            if (eventName.getText() != null && eventName.getText().length() > 0) {
                jsonObject.put("name", eventName.getText());
            }
            if (date.getText() != null && date.getText().length() > 0) {
                jsonObject.put("date", date.getText());
            }
            if (time.getText() != null && time.getText().length() > 0) {
                jsonObject.put("time", time.getText());
            }
            if (description.getText() != null && description.getText().length() > 0) {
                jsonObject.put("description", description.getText());
            }
            if (allDay.isChecked() != curAllDay) {
                jsonObject.put("all_day", allDay.isChecked());
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        //check if jsonObject is empty, return error if is empty
        if (jsonObject.length() == 0) {
            Toast.makeText(getApplicationContext(), "Error: At least one field is required", Toast.LENGTH_LONG).show();
        }
        else {
            //set request body
            RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

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
                    String r = response.body().string();

                    try {
                        JSONObject resp = new JSONObject(r);

                        //check for error
                        if (resp.has("error")) {
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
                        //create new bundle, direct back to event page
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Updated event", Toast.LENGTH_LONG).show();
                                }
                            });

                            //remove unneccessary info in bundle
                            mBundle.remove("name");
                            mBundle.remove("date");
                            mBundle.remove("time");
                            mBundle.remove("description");
                            mBundle.remove("all_day");

                            Intent intent = new Intent(UpdateEvent.this, SingleEventPage.class);
                            intent.putExtras(mBundle);
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
