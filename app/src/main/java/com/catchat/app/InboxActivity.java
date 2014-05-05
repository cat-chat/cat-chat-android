package com.catchat.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;


public class InboxActivity extends Activity {

    private static final int CONTACT_PICKER_RESULT = 1;
    public static final int IMAGE_PICKER = 2;
    private int mImageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inbox);

        if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser()) && currentUserHasNoEmailAddress()) {
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
            startNewMessage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startNewMessage() {
        startActivityForResult(new Intent(this, CatImagePickerActivity.class), IMAGE_PICKER);
    }

    private void getEmailAddressFromContacts() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        contactPickerIntent.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER) {
            Bundle extras = data.getExtras();
            mImageId = extras.getInt("imageid");

            getEmailAddressFromContacts();
        } else if (resultCode == RESULT_OK && requestCode == CONTACT_PICKER_RESULT) {
            Contact c = getEmailAddress(data);

            if (c != null) {
                ParseObject gameScore = new ParseObject("PendingMessage");
                gameScore.put("fromUser", ParseUser.getCurrentUser());
                gameScore.put("toEmail", c.email());
                gameScore.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.e("CatChatTag", "lol", e);
                    }
                });
            }
        }
    }

    private Contact getEmailAddress(Intent data) {
        Cursor cursor = null;
        Contact c = null;
        try {
            Uri result = data.getData();
            String id = result.getLastPathSegment();

            cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone._ID + " = ?",
                    new String[] { id },
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String emailAddress = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                c = new Contact(name, emailAddress);
            }
        } catch (Exception e) {
            Log.e("CatChatTag", "Failed to get email data", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return c;
    }
}
