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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by Chermaine on 8/9/17.
 */

public class UserProfileActivity extends AppCompatActivity {
    private Bundle mBundle;
    private OkHttpClient mOkHttpClient;

    private TextView firstName;
    private TextView lastName;
    private TextView username;
    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //get user info from Bundle
        mBundle = this.getIntent().getExtras();

        Log.i("UserProfile:", String.format("UserProfile onCreate bundle = %s", mBundle.toString()));

        //get TextViews
        firstName = (TextView) findViewById(R.id.profile_first_name);
        lastName = (TextView) findViewById(R.id.profile_last_name);
        username = (TextView) findViewById(R.id.profile_username);
        email = (TextView) findViewById(R.id.profile_email);

        //set info to appropriate EditText
        firstName.setText(mBundle.getString("first_name"));
        lastName.setText(mBundle.getString("last_name"));
        username.setText(mBundle.getString("username"));
        email.setText(mBundle.getString("email"));

        //add event listener to buttons
        Button updateProfile = (Button) findViewById(R.id.update_profile_button);
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();
            }
        });

        Button changePassword = (Button) findViewById(R.id.change_password);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeUserPassword();
            }
        });

        Button deleteAccount = (Button) findViewById(R.id.delete_user_account);
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUserAccount();
            }
        });
    }

    //direct user to update profile activity
    protected void updateUserProfile() {
        Intent intent = new Intent(UserProfileActivity.this, UpdateUserProfile.class);
        intent.putExtras(mBundle);
        startActivity(intent);
    }

    //direct user to change password activity
    protected void changeUserPassword() {
        Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
        intent.putExtras(mBundle);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_new_event:
                //direct to add new event activity
                Intent intent = new Intent(UserProfileActivity.this, AddNewEventActivity.class);
                intent.putExtras(mBundle);
                startActivity(intent);
                return true;

            case R.id.action_show_past_event:
                //direct to user past event activity
                Intent history = new Intent(UserProfileActivity.this, ShowPastEventActivity.class);
                history.putExtras(mBundle);
                startActivity(history);
                return true;

            case R.id.action_logout:
                //logout and direct to main activity
                Intent main = new Intent(UserProfileActivity.this, MainActivity.class);
                startActivity(main);
                return true;

            case R.id.action_home:
                //direct to home page
                Intent home = new Intent(UserProfileActivity.this, HomePageActivity.class);
                home.putExtras(mBundle);
                startActivity(home);
                return true;

            case R.id.action_user_profile:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void deleteUserAccount() {
        mOkHttpClient = new OkHttpClient();

        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/users/" + mBundle.getString("id"));

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
                            Toast.makeText(getApplicationContext(), "Account deleted", Toast.LENGTH_LONG).show();
                        }
                    });

                    Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}
