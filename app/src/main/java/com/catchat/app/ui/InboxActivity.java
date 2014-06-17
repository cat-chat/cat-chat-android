package com.catchat.app.ui;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.catchat.app.R;
import com.catchat.app.Utils;
import com.catchat.app.data.CatChatContentProvider;
import com.catchat.app.ui.auth.MainActivity;
import com.catchat.app.ui.sendingmessage.CatImagePickerActivity;
import com.catchat.app.ui.sendingmessage.SendCatMessageActivity;
import com.facebook.widget.FacebookDialog;
import com.negusoft.holoaccent.activity.AccentActivity;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RefreshCallback;

import java.util.HashMap;
import java.util.List;


public class InboxActivity extends AccentActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, View.OnClickListener {

    private static final int IMAGE_PICKER = 1;

    private ListView mListView;
    private View mEmptyViews;
    private View mResendVerificationEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inbox);

        mResendVerificationEmailButton = findViewById(R.id.resend_verification_email);
        mResendVerificationEmailButton.setOnClickListener(this);

        mEmptyViews = findViewById(R.id.empty_layout);
        mListView = (ListView) findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);

        if (!currentUserHasVerifiedTheirEmailAddress()) {
            showEmailNotVerifiedWarning();
        } else {
            mResendVerificationEmailButton.setVisibility(View.GONE);

            initialiseMessagesAdapter();
        }
//        CatProgressDialog.show(this, getString(R.string.retrieving_cat_pics));
    }

    private void showEmailNotVerifiedWarning() {
        TextView emptyTitle = (TextView) mEmptyViews.findViewById(R.id.empty_title);
        TextView emptyContent = (TextView) mEmptyViews.findViewById(R.id.empty_content);

        emptyTitle.setText(R.string.please_verify_email);
        emptyContent.setText(R.string.please_verify_email_message);
    }

    private void showNoMessagesInformation() {
        TextView emptyTitle = (TextView) mEmptyViews.findViewById(R.id.empty_title);
        TextView emptyContent = (TextView) mEmptyViews.findViewById(R.id.empty_content);

        emptyTitle.setText(R.string.no_messages);
        emptyContent.setText(R.string.tap_plus_to_send_message);
    }

    private void initialiseMessagesAdapter() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.contact_row,
                null,
                new String[]{CatChatContentProvider.MESSAGE_FROM_USER_EMAIL, CatChatContentProvider.MESSAGE_FROM_USER_PARSE_ID},
                new int[]{R.id.contact_textview},
                android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mListView.setAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ParseUser.getCurrentUser().refreshInBackground(new RefreshCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    refresh();
                }
            }
        });
    }

    private void refresh() {
        if (currentUserHasVerifiedTheirEmailAddress()) {
            mResendVerificationEmailButton.setVisibility(View.GONE);

            showNoMessagesInformation();

            if (mListView.getAdapter() == null) {
                initialiseMessagesAdapter();
            }

            refreshMessages();
        }
    }

    private boolean currentUserHasVerifiedTheirEmailAddress() {
        return ParseUser.getCurrentUser().getBoolean("emailVerified");
    }

    private void refreshMessages() {
        ParseQuery<ParseObject> queryMessages = ParseQuery.getQuery("Message");
        queryMessages.whereEqualTo("toUser", ParseUser.getCurrentUser());
        queryMessages.include("fromUser");
        queryMessages.orderByAscending("createdAt");
        queryMessages.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                if (messages == null || e != null) {
                    Log.d("CatChatTag", "No messages found", e);
                    return;
                }

                for (ParseObject m : messages) {
                    ContentValues v = new ContentValues();
                    v.put(CatChatContentProvider.MESSAGE_PARSE_ID, m.getObjectId());
                    v.put(CatChatContentProvider.MESSAGE_IMAGE_ID, m.getParseObject("image").getObjectId());
                    v.put(CatChatContentProvider.MESSAGE_FROM_USER_PARSE_ID, m.getParseObject("fromUser").getObjectId());
                    v.put(CatChatContentProvider.MESSAGE_FROM_USER_EMAIL, m.getParseObject("fromUser").getString("email"));
                    v.put(CatChatContentProvider.MESSAGE_CONTENTS, m.getString("messageData"));
                    v.put(CatChatContentProvider.MESSAGE_SENT_TIME, Utils.formatDate(m.getCreatedAt()));

                    Uri uri = getContentResolver().insert(CatChatContentProvider.MESSAGES_TABLE_GROUP_BY_FROM_USER_ID_URI, v);

                    if (uri.equals(Uri.withAppendedPath(CatChatContentProvider.MESSAGES_TABLE_GROUP_BY_FROM_USER_ID_URI, Long.toString(1)))) {
                        m.deleteEventually();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inbox, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_add).setVisible(currentUserHasVerifiedTheirEmailAddress());
        menu.findItem(R.id.invite).setVisible(buildInviteFriendsDialog().canPresent());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            startNewMessage();
            return true;
        } else if (id == R.id.invite) {
            FacebookDialog.MessageDialogBuilder builder = buildInviteFriendsDialog();

            // If the Messaging Facebook app is installed and we can present the share dialog
            if (builder.canPresent()) {
                FacebookDialog dialog = builder.build();
                dialog.present();
                return true;
            }
        } else if (id == R.id.sent_messages) {
            Intent intent = new Intent(this, ConversationActivity.class);
            intent.putExtra("sentMessages", true);
            startActivity(intent);
        } else if (id == R.id.logout) {
            ParseUser.logOut();
            ParseQuery.clearAllCachedResults();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private FacebookDialog.MessageDialogBuilder buildInviteFriendsDialog() {
        return new FacebookDialog.MessageDialogBuilder(this)
                .setLink("www.catchatapp.com")
                .setName(getString(R.string.fb_msg_name))
                .setCaption(getString(R.string.fb_msg_caption))
                .setDescription(getString(R.string.fb_msg_description))
                .setPicture("http://bit.ly/RPsDPY");
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
        return new CursorLoader(this, CatChatContentProvider.MESSAGES_TABLE_GROUP_BY_FROM_USER_ID_URI, new String[]{CatChatContentProvider.MESSAGE_FROM_USER_EMAIL, CatChatContentProvider.MESSAGE_FROM_USER_PARSE_ID, CatChatContentProvider.ID}, null, null, null);
    }

    private SimpleCursorAdapter getAdapter() {
        return ((SimpleCursorAdapter) mListView.getAdapter());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor c) {
        Log.d("CatChatTag", "InboxActivity, found: " + c.getCount());
        getAdapter().swapCursor(c);

        if (c != null && c.getCount() > 0) {
            mListView.setVisibility(View.VISIBLE);
            mEmptyViews.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        getAdapter().swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor c = (Cursor) adapterView.getAdapter().getItem(i);

        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra("fromUserId", c.getString(c.getColumnIndex(CatChatContentProvider.MESSAGE_FROM_USER_PARSE_ID)));
        intent.putExtra("fromUserEmail", c.getString(c.getColumnIndex(CatChatContentProvider.MESSAGE_FROM_USER_EMAIL)));

        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.resend_verification_email) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("userid", ParseUser.getCurrentUser().getObjectId());

            ParseCloud.callFunctionInBackground("resendVerificationEmail", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    if (e == null) {
                        Toast.makeText(InboxActivity.this, getString(R.string.email_verification_sent), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(InboxActivity.this, getString(R.string.failed_to_send_email_verification) + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
