package com.catchat.app.ui;

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
import com.catchat.app.Utils;
import com.negusoft.holoaccent.activity.AccentActivity;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUpActivity extends AccentActivity implements View.OnClickListener, TextWatcher {

    private Button mSignUpButton;
    private Dialog mProgressDialog;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        mSignUpButton = (Button) findViewById(R.id.signup);
        mSignUpButton.setOnClickListener(this);

        mEmailEditText = (EditText)findViewById(R.id.email);
        mPasswordEditText = (EditText)findViewById(R.id.password);

        mEmailEditText.addTextChangedListener(this);
        mPasswordEditText.addTextChangedListener(this);

        setSignUpButtonEnabledState();
    }

    private void setSignUpButtonEnabledState() {
        mSignUpButton.setEnabled(getEmailAddress().length() > 0 && getPassword().length() > 0);
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

        mProgressDialog = CatProgressDialog.show(SignUpActivity.this, getString(R.string.signing_up));

        ParseUser user = new ParseUser();
        user.setUsername(emailAddress);
        user.setPassword(password);
        user.setEmail(emailAddress);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                mProgressDialog.dismiss();

                if (e == null) {
                    Utils.mapInstallationToCurrentUser();
                    startActivity(new Intent(SignUpActivity.this, InboxActivity.class));
                    finish();
                } else {
                    mEmailEditText.setError(e.getMessage());
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
        setSignUpButtonEnabledState();
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
