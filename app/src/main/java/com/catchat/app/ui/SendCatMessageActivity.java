package com.catchat.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.catchat.app.Contact;
import com.catchat.app.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class SendCatMessageActivity extends Activity implements View.OnTouchListener {
    private static final int CONTACT_PICKER_RESULT = 2;
    private ImageView mImageView;
    private String mImageId;

    private EditText mTopEditText;
    private EditText mBottomEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cat_message);

        mImageView = (ImageView) findViewById(R.id.imageview);
        mTopEditText = (EditText) findViewById(R.id.top_edittext);
        mBottomEditText = (EditText) findViewById(R.id.bottom_edittext);

        Typeface tf = Typeface.createFromAsset(getAssets(), "LondrinaSolid-Regular.otf");
        mTopEditText.setTypeface(tf);
        mBottomEditText.setTypeface(tf);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        layout.setOnTouchListener(this);

        Bundle extras = getIntent().getExtras();
        mImageId = extras.getString("imageid");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("CatImage");
        query.fromLocalDatastore();
        query.whereEqualTo("objectId", mImageId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null && parseObjects.size() > 0) {
                    ParseFile file = parseObjects.get(0).getParseFile("image");
                    try {
                        Bitmap bmp = BitmapFactory.decodeByteArray(file.getData(), 0, file.getData().length);
                        mImageView.setImageBitmap(bmp);
                    } catch (ParseException ex) {
                        Log.d("CatChatTag", "Failed to decode bitmap", ex);
                    }
                } else {
                    Log.e("CatChatTag", "Error pulling cat pic", e);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CONTACT_PICKER_RESULT) {
            Contact c = getEmailAddress(data);

            if (c != null) {
                ParseObject dummyImage = ParseObject.createWithoutData("CatImage", mImageId);

                ParseObject pendingMessage = new ParseObject("PendingMessage");
                pendingMessage.put("fromUser", ParseUser.getCurrentUser());
                pendingMessage.put("image", dummyImage);
                pendingMessage.put("messageData", getMessageData());
                pendingMessage.put("toEmail", c.email());
                pendingMessage.saveEventually();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getMessageData() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("top", mTopEditText.getText().toString().trim());
        map.put("bottom", mBottomEditText.getText().toString().trim());

        return new JSONObject(map).toString();
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
                    new String[]{id},
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_mesage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.send) {
            getEmailAddressFromContacts();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getEmailAddressFromContacts() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        contactPickerIntent.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        hideKeyboard(view);
        return false;
    }

    protected void hideKeyboard(View view) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
