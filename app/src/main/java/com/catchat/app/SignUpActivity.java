package com.catchat.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUpActivity extends Activity implements View.OnClickListener {

    private Button mSignUpButton;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        mSignUpButton = (Button) findViewById(R.id.signup);
        mSignUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        EditText emailEditText = (EditText)findViewById(R.id.email);
        EditText passwordEditText = (EditText)findViewById(R.id.email);

        String emailAddress = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mProgressDialog = ProgressDialog.show(SignUpActivity.this, "", "Signing up...", true);

        ParseUser user = new ParseUser();
        user.setUsername(emailAddress);
        user.setPassword(password);
        user.setEmail(emailAddress);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                mProgressDialog.dismiss();

                if (e == null) {
                    startActivity(new Intent(SignUpActivity.this, InboxActivity.class));
                } else {
                    Log.e("CatChatSignUp", "Failed to signup", e);
                }
            }
        });
    }
}
