package com.catchat.app;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class CatChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));
    }
}
