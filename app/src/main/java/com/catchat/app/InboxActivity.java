package com.catchat.app;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;


public class InboxActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inbox);

        if(ParseFacebookUtils.isLinked(ParseUser.getCurrentUser()) && currentUserHasNoEmailAddress()) {
            retrieveEmailAddress();
        }
    }

    private boolean currentUserHasNoEmailAddress() {
        return TextUtils.isEmpty(ParseUser.getCurrentUser().getEmail());
    }

    private void retrieveEmailAddress() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {

                            try {
                                String email = user.getInnerJSONObject().get("email").toString();

                                ParseUser.getCurrentUser().setEmail(email);
                                ParseUser.getCurrentUser().saveEventually();
                            } catch (JSONException e) {
                                Log.e("CatChatInbox", "Failed to parse JSON from FB", e);
                            }

                        }
                    }
                }
        );
        request.executeAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inbox, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {


            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}