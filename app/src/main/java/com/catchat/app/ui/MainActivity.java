package com.catchat.app.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.catchat.app.CatChatApplication;
import com.catchat.app.R;
import com.catchat.app.Utils;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

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

        ParseAnalytics.trackAppOpened(getIntent());

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
        mProgressDialog = ProgressDialog.show(MainActivity.this, "", getString(R.string.logging_in), true);

        List<String> permissions = Utils.getFBPermissions();
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                MainActivity.this.mProgressDialog.dismiss();
                if (user == null) {
                    Log.d("CatChatLogin", "User cancelled Facebook login");
                } else {
                    if(currentUserHasNoEmailAddress()) {
                        ((CatChatApplication)getApplication()).retrieveEmailAddress();
                    }

                    Utils.mapInstallationToCurrentUser();
                    showInboxActivity();
                }
            }
        });
    }

    private boolean currentUserHasNoEmailAddress() {
        return TextUtils.isEmpty(ParseUser.getCurrentUser().getEmail());
    }

    private void showInboxActivity() {
        startActivity(new Intent(this, InboxActivity.class));
        finish();
    }
}
