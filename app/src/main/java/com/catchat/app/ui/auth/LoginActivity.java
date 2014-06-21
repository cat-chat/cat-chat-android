package com.catchat.app.ui.auth;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.catchat.app.R;
import com.catchat.app.Utils;
import com.catchat.app.ui.InboxActivity;
import com.catchat.app.ui.progress.CatProgressDialog;
import com.negusoft.holoaccent.activity.AccentActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import java.util.List;

public class LoginActivity extends AccentActivity implements View.OnClickListener, TextWatcher {

    private Button mForgotPassword;
    private Button mLoginButton;
    private Dialog mProgressDialog;

    private EditText mPasswordEditText;
    private AutoCompleteTextView mEmailEditText;

    private boolean mResetPasswordInitialised = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mForgotPassword = (Button) findViewById(R.id.forgotpassword);
        mForgotPassword.setOnClickListener(this);

        mLoginButton = (Button) findViewById(R.id.login);
        mLoginButton.setOnClickListener(this);

        mEmailEditText = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordEditText = (EditText) findViewById(R.id.password);

        mEmailEditText.addTextChangedListener(this);
        mPasswordEditText.addTextChangedListener(this);

        setLoginButtonEnabledState();

        List<String> emails = Utils.getPrimaryEmailAddresses(this);
        mEmailEditText.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, emails));
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
        if (view.getId() == R.id.login) {
            if (mResetPasswordInitialised) {
                resetPassword();
            } else {
                login();
            }
        } else if (view.getId() == R.id.forgotpassword) {
            onForgotPasswordTapped();
        }
    }

    private void resetPassword() {
        ParseUser.requestPasswordResetInBackground(getEmailAddress(), new RequestPasswordResetCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(LoginActivity.this, R.string.password_reset_email_sent, Toast.LENGTH_SHORT).show();

                    mResetPasswordInitialised = false;
                    mLoginButton.setText(R.string.log_in);
                    mPasswordEditText.animate().setDuration(500).alpha(1);
                } else {
                    mEmailEditText.setError(e.getMessage());
                }
            }
        });
    }

    private void login() {
        String emailAddress = getEmailAddress();
        String password = getPassword();

        mProgressDialog = CatProgressDialog.show(LoginActivity.this, getString(R.string.logging_in));

        ParseUser.logInInBackground(emailAddress, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                mProgressDialog.dismiss();

                if (e == null) {
                    Utils.mapInstallationToCurrentUser();
                    startActivity(new Intent(LoginActivity.this, InboxActivity.class));
                    finish();
                } else {
                    if (mResetPasswordInitialised) {
                        // we're resetting pw so the only error can possibly be with the email address entered
                        mEmailEditText.setError(e.getMessage());
                    } else {
                        mPasswordEditText.setError(e.getMessage());
                    }
                    Log.e("CatChatSignUp", "Failed to signup", e);
                }
            }
        });
    }

    private void onForgotPasswordTapped() {
        if (!mResetPasswordInitialised) {
            mLoginButton.setText(R.string.reset_password);
            mPasswordEditText.setText("");
            mPasswordEditText.setError(null);
            mPasswordEditText.animate().setDuration(500).alpha(0);
            mLoginButton.setEnabled(true);
            mResetPasswordInitialised = true;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        if (!mResetPasswordInitialised) {
            setLoginButtonEnabledState();
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
