package com.catchat.app.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.catchat.app.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends Activity implements View.OnClickListener {

    private Button mLoginButton;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mLoginButton = (Button) findViewById(R.id.login);
        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        EditText emailEditText = (EditText)findViewById(R.id.email);
        EditText passwordEditText = (EditText)findViewById(R.id.email);

        String emailAddress = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mProgressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...", true);

        ParseUser.logInInBackground(emailAddress, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                mProgressDialog.dismiss();

                if (e == null) {
                    startActivity(new Intent(LoginActivity.this, InboxActivity.class));
                    finish();
                } else {
                    Log.e("CatChatSignUp", "Failed to signup", e);
                }
            }
        });
    }
}
