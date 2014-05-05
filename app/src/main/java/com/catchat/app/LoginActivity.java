package com.catchat.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends Activity implements View.OnClickListener {

    private Button mFacebookButton;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mFacebookButton = (Button) findViewById(R.id.facebook);
        mFacebookButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.facebook) {
            onLoginButtonClicked();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    private void onLoginButtonClicked() {
        mProgressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...", true);

        List<String> permissions = Arrays.asList("email");
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                LoginActivity.this.mProgressDialog.dismiss();
                if (user == null) {
                    Log.d("CatChatLogin", "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("CatChatLogin", "User signed up and logged in through Facebook!");
                    showInboxActivity();
                } else {
                    Log.d("CatChatLogin", "User logged in through Facebook!");
                    showInboxActivity();
                }
            }
        });
    }

    private void showInboxActivity() {
        startActivity(new Intent(this, InboxActivity.class));
    }
}
