package com.catchat.app;

import android.app.Application;

import com.catchat.app.ui.InboxActivity;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.PushService;

import java.util.List;

public class CatChatApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        PushService.setDefaultPushCallback(this, InboxActivity.class);

        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));

        pullAndCacheLatestCatPics();
    }

    private void pullAndCacheLatestCatPics() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CatImage");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> catImages, ParseException e) {
                if (e == null && catImages != null) {
                    ParseObject.pinAllInBackground(catImages);
                }
            }
        });
    }
}
