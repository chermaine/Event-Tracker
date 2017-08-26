package com.example.chermaine.a496finalhybridproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
 * Created by Chermaine on 8/10/17.
 */

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmNewPassword;
    private CheckBox showOldPass;
    private CheckBox showNewPass;
    private CheckBox showConfirmPass;
    private Bundle bundle;
    private OkHttpClient okHttpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        //get info from intent's bundle
        bundle = this.getIntent().getExtras();

        Log.i("ChangePassword:", String.format("ChangePassword onCreate bundle = %s", bundle.toString()));

        //handle show password checkboxes
        showOldPass = (CheckBox) findViewById(R.id.show_current_password);
        showNewPass = (CheckBox) findViewById(R.id.show_new_password);
        showConfirmPass = (CheckBox) findViewById(R.id.show_confirm_password);
        oldPassword = (EditText) findViewById(R.id.input_current_password);
        newPassword = (EditText) findViewById(R.id.input_new_password);
        confirmNewPassword = (EditText) findViewById(R.id.input_confirm_password);
        showPasswordHandler();

        //add event listener to submit button
        Button submit = (Button) findViewById(R.id.submit_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

        //add event listener to cancel button
        Button cancel = (Button) findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //bring user back to user profile activity
                Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    //compare 2 strings - return true if they are equal, else return false
    protected boolean compareStrings(String s1, String s2) {
        if (s1.equals(s2)) {
            return true;
        }
        else {
            return false;
        }
    }

    //validates input entered by user when submit button is clicked
    //1 - make sure all required fills are filled
    //2 - make sure current password entered by user is the same as current password in user's account
    //3 - make sure new password entered are the same
    //4 - make sure new password is not the same as current password
    //return true if all inputs are valid
    //return false if either of the above occurred
    protected boolean validateInputs() {
        //validate all inputs are filled
        if (TextUtils.isEmpty(oldPassword.getText().toString()) ||
                TextUtils.isEmpty(newPassword.getText().toString()) ||
                TextUtils.isEmpty(confirmNewPassword.getText().toString())){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Error: All fields are required", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }

        //validate that oldPassword entered is same as user's account password
        //if not equal report error
        Log.i("current pass: " , bundle.getString("password"));
        if (!compareStrings(oldPassword.getText().toString(), bundle.getString("password"))) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Error: Current password does not match", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }

        //validate that new password and confirm new password is equal
        //if not equal report error
        if (!compareStrings(newPassword.getText().toString(), confirmNewPassword.getText().toString())){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Error: New password does not match", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }

        //validate that new password is not the same as old password
        if (compareStrings(newPassword.getText().toString(), bundle.getString("password"))) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Error: New password cannot be the same as current password", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }

        //no error, return true
        return true;
    }

    //handle the changing of password when submit button is clicked
    //1 - validate all user inputs
    //2 - send PATCH request to API to update password
    //3 - direct user back to user profile page if password is updated
    protected void changePassword() {
        if (!validateInputs()) {
            return;
        }

        //create new http client
        okHttpClient = new OkHttpClient();

        //set request url
        HttpUrl reqUrl = HttpUrl.parse("https://final-project-176122.appspot.com/users/" + bundle.getString("id"));

        //set media type
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        //set request body
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("password", newPassword.getText());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        //set request
        Request request = new Request.Builder()
                .url(reqUrl)
                .patch(requestBody)
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
                    final JSONObject resp = new JSONObject(r);

                    //check if error occurred
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

                    //no error, go back to profile page
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Updated password", Toast.LENGTH_LONG).show();
                            }
                        });

                        //create a new bundle
                        Bundle newBundle = createNewBundle(resp);
                        Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
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

    //create a new bundle with new information after receiving an response from PATCH request
    protected Bundle createNewBundle(JSONObject resp) {
        Bundle b = new Bundle();
        try {
            //save a copy of all information into bundle
            if (resp.has("first_name")) {
                b.putString("first_name", resp.getString("first_name"));
            }
            if (resp.has("last_name")) {
                b.putString("last_name", resp.getString("last_name"));
            }
            if (resp.has("username")) {
                b.putString("username", resp.getString("username"));
            }
            if (resp.has("email")) {
                b.putString("email", resp.getString("email"));
            }
            if (resp.has("password")){
                b.putString("password", resp.getString("password"));
            }
            if (resp.has("events")) {
                b.putString("events", resp.getString("events"));
            }
            if (resp.has("id")) {
                b.putString("id", resp.getString("id"));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        //return bundle
        return b;
    }

    //display or hide password entered by user if checkboxes are checked
    protected void showPasswordHandler() {
        //add on checkedlistener for checkboxes
        showOldPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            //show password
            if (!isChecked) {
                oldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            //hide password
            else {
                oldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            }
        });

        //add on checkedlistener for checkbox
        showNewPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            //show password
            if (!isChecked) {
                newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            //hide password
            else {
                newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            }
        });

        //add on checkedlistener for checkbox
        showConfirmPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            //show password
            if (!isChecked) {
                confirmNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            //hide password
            else {
                confirmNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
            }
        });
    }
}
