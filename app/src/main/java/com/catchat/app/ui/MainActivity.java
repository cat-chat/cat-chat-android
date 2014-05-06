package com.catchat.app.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.catchat.app.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button mFacebookButton;
    private Button mSignUpButton;
    private Button mLoginButton;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mFacebookButton = (Button) findViewById(R.id.facebook);
        mFacebookButton.setOnClickListener(this);

        mSignUpButton = (Button) findViewById(R.id.signup);
        mSignUpButton.setOnClickListener(this);

        mLoginButton = (Button) findViewById(R.id.login);
        mLoginButton.setOnClickListener(this);

        if (ParseUser.getCurrentUser() != null) {
            showInboxActivity();
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.facebook) {
            onLoginButtonClicked();
        } else if (view.getId() == R.id.signup) {
            startActivity(new Intent(this, SignUpActivity.class));
        } else if (view.getId() == R.id.login) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    private void onLoginButtonClicked() {
        mProgressDialog = ProgressDialog.show(MainActivity.this, "", "Logging in...", true);

        List<String> permissions = Arrays.asList("email");
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                MainActivity.this.mProgressDialog.dismiss();
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
        finish();
    }
}
