package com.example.chermaine.a496finalhybridproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {
    CallbackManager callbackManager;
    String[] permissions = {"public_profile", "email"};

    private EditText password;
    private EditText username;
    private CheckBox showPassword;
    private OkHttpClient mOkHttpClient;
    private AccessTokenTracker accessTokenTracker;
    private LoginButton fbLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create new Http client
        mOkHttpClient = new OkHttpClient();

        //facebook login
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        fbLoginButton.setReadPermissions(permissions);

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

        //check if user has logged in
        checkFBLogin();
        updateWithToken(AccessToken.getCurrentAccessToken());

        //add event listener to buttons
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = (EditText) findViewById(R.id.input_username);
                password = (EditText) findViewById(R.id.input_password);

                JSONObject credentials = new JSONObject();
                try {
                    credentials.put("username", username.getText());
                    credentials.put("password", password.getText());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                //check login credentials
                checkLoginCredential(credentials);
            }
        });

        Button signupButton = (Button) findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change view to display signup page
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    protected void checkFBLogin() {
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                updateWithToken(currentAccessToken);
            }
        };
    }

    protected void getUserFBProfile(AccessToken accessToken) {
        if (accessToken !=  null) {
            GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    //direct to fb home page
                    Bundle fbBundle = new Bundle();
                    try {
                        fbBundle.putString("email", object.getString("email"));
                        fbBundle.putString("id", object.getString("id"));
                        fbBundle.putString("first_name", object.getString("first_name"));
                        fbBundle.putString("last_name", object.getString("last_name"));
                        fbBundle.putBoolean("fb_login", true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(MainActivity.this, FBHomePage.class);
                    intent.putExtras(fbBundle);
                    startActivity(intent);
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "first_name, last_name, email, id, link");
            request.setParameters(parameters);
            request.executeAsync();
        }
        else {
            fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();

                    getUserFBProfile(loginResult.getAccessToken());
                }
                /*GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        //direct to fb home page
                        Bundle fbBundle = new Bundle();
                        try {
                            fbBundle.putString("email", object.getString("email"));
                            fbBundle.putString("id", object.getString("id"));
                            fbBundle.putString("first_name", object.getString("first_name"));
                            fbBundle.putString("last_name", object.getString("last_name"));
                            fbBundle.putBoolean("fb_login", true);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(MainActivity.this, FBHomePage.class);
                        intent.putExtras(fbBundle);
                        startActivity(intent);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name, last_name, email, id, link");
                request.setParameters(parameters);
                request.executeAsync();
            }*/

                @Override
                public void onCancel() {
                    Toast.makeText(getApplicationContext(), "User cancelled login process!", Toast.LENGTH_LONG).show();
                    Log.i("FBLogin: ", "User cancelled login process");
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(getApplicationContext(), String.format("Error login: %s", error), Toast.LENGTH_LONG).show();
                    Log.i("FBLogin: ", String.format("Error login: %s", error));
                }
            });

        }
    }

    protected void updateWithToken(AccessToken currentAccessToken) {
        //logout user
        if (currentAccessToken != null) {
            //get user fb profile
            getUserFBProfile(currentAccessToken);

            /*new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                    .Callback() {
                @Override
                public void onCompleted(GraphResponse graphResponse) {

                    LoginManager.getInstance().logOut();

                }
            }).executeAsync();*/
        }
    }

    //check user entered username and password
    //send POST to /user with username and password entered
    //if credentials are valid, direct user to home page
    //else report error
    protected void checkLoginCredential(JSONObject credentials) {
        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/user");

        //set request body
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, credentials.toString());

        //set request
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
                //get response
                String r = response.body().string();
                Log.i("onResponse: ", String.format("response = %s", r));

                try {
                    final JSONObject resp = new JSONObject(r);
                    Log.i("resp: ", String.format("resp = %s", resp.toString()));

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
                        Log.i("error", resp.getString("error"));
                    }
                    else {
                        //create a bundle to pass to next activity
                        Bundle bundle = createBundle(resp);

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

    //create a bundle with information of user account to be sent to next activity
    protected Bundle createBundle(JSONObject object) {
        Bundle b = new Bundle();

        try {
            b.putString("first_name", object.getString("first_name"));
            b.putString("last_name", object.getString("last_name"));
            b.putString("username", object.getString("username"));
            b.putString("email", object.getString("email"));
            b.putString("password", object.getString("password"));
            b.putString("events", object.getString("events"));
            b.putString("id", object.getString("id"));
            b.putBoolean("fb_login", false);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return b;
    }
}
