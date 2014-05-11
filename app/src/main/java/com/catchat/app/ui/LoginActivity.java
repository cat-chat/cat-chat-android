package com.catchat.app.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.catchat.app.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends Activity implements View.OnClickListener, TextWatcher {

    private Button mLoginButton;
    private Dialog mProgressDialog;

    private EditText mPasswordEditText;
    private EditText mEmailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mLoginButton = (Button) findViewById(R.id.login);
        mLoginButton.setOnClickListener(this);

        mEmailEditText = (EditText) findViewById(R.id.email);
        mPasswordEditText = (EditText) findViewById(R.id.password);

        mEmailEditText.addTextChangedListener(this);
        mPasswordEditText.addTextChangedListener(this);

        setLoginButtonEnabledState();
    }

    private void setLoginButtonEnabledState() {
        mLoginButton.setEnabled(getEmailAddress().length() > 0 && getPassword().length() > 0);
    }

    private String getPassword() {
        return mPasswordEditText.getText().toString().trim();
    }

    private String getEmailAddress() {
        return mEmailEditText.getText().toString().trim();
    }

    @Override
    public void onClick(View view) {
        String emailAddress = getEmailAddress();
        String password = getPassword();

        mProgressDialog = ProgressDialog.show(LoginActivity.this, "", getString(R.string.logging_in), true);

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

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        setLoginButtonEnabledState();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
