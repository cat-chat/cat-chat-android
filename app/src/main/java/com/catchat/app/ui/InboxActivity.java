package com.catchat.app.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.catchat.app.CatChatContentProvider;
import com.catchat.app.R;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

import java.util.List;


public class InboxActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int IMAGE_PICKER = 1;

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inbox);

        if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser()) && currentUserHasNoEmailAddress()) {
            retrieveEmailAddress();
        }

        refreshMessages();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.contact_row,
                null,
                new String[]{CatChatContentProvider.MESSAGE_FROM_USER_EMAIL},
                new int[]{R.id.contact_textview},
                android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        getLoaderManager().initLoader(0, null, this);

        mListView = (ListView) findViewById(R.id.listview);
        mListView.setAdapter(adapter);
    }

    private void refreshMessages() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.include("fromUser");
        query.whereEqualTo("toUser", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                for (ParseObject m : messages) {
                    ContentValues v = new ContentValues();
                    v.put(CatChatContentProvider.MESSAGE_PARSE_ID, m.getObjectId());
                    v.put(CatChatContentProvider.MESSAGE_FROM_USER_EMAIL, m.getParseObject("fromUser").getString("email"));
                    v.put(CatChatContentProvider.MESSAGE_CONTENTS, m.getString("messageData"));
                    v.put(CatChatContentProvider.MESSAGE_SENT_TIME, m.getCreatedAt().toString());

                    getContentResolver().insert(CatChatContentProvider.MESSAGES_TABLE_URI, v);
                }
            }
        });
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
            startNewMessage();
            return true;
        } else if (id == R.id.logout) {
            ParseUser.logOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startNewMessage() {
        startActivityForResult(new Intent(this, CatImagePickerActivity.class), IMAGE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER) {
            Bundle extras = data.getExtras();
            String imageid = extras.getString("imageid");

            Intent intent = new Intent(this, SendCatMessageActivity.class);
            intent.putExtra("imageid", imageid);
            startActivity(intent);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, CatChatContentProvider.MESSAGES_TABLE_URI,new String[]{CatChatContentProvider.MESSAGE_FROM_USER_EMAIL, CatChatContentProvider.ID}, null, null, null);
    }

    private SimpleCursorAdapter getAdapter() {
        return ((SimpleCursorAdapter)mListView.getAdapter());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor c) {
        getAdapter().swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        getAdapter().swapCursor(null);
    }
}
