package com.catchat.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.catchat.app.Contact;
import com.catchat.app.R;
import com.facebook.Session;
import com.facebook.SessionState;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SendCatMessageActivity extends Activity implements View.OnTouchListener {
    private static final int CONTACT_PICKER_RESULT = 2;
    private static final int PICK_FB_CONTACT = 3;

    private ImageView mImageView;
    private String mImageId;
    private Dialog mProgressDialog;

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
                sendMessage("toEmail", c.email());
            }
        } else if(resultCode == RESULT_OK && requestCode == PICK_FB_CONTACT) {
            String fbId = data.getExtras().getString("fbid");

            if(!TextUtils.isEmpty(fbId)) {
                sendMessage("toFacebook", fbId);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void sendMessage(String toField, String toFieldValue) {
        try {
            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("fromUser", ParseUser.getCurrentUser().getObjectId());
            map.put("image", mImageId);
            map.put("messageData", getMessageData());
            map.put(toField, toFieldValue);
            Log.d("CatChatTag", new JSONObject(map).toString());

            ParseCloud.callFunctionInBackground("sendMessage", map, new FunctionCallback<Object>() {
                @Override
                public void done(Object o, ParseException e) {
                    Log.d("CatChatTag", "callback: " + o, e);
                }
            });

            finish();
        } catch (Exception e) {
            Log.e("CatChatTag", "Failed to create message", e);
            Toast.makeText(this, "Failed to create Message :-(", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMessageData() throws JSONException {
        HashMap<String, Object> topObject = buildJsonFromEditText(mTopEditText);
        HashMap<String, Object> bottomObject = buildJsonFromEditText(mBottomEditText);

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("top", topObject);
        map.put("bottom", bottomObject);

        return new JSONObject(map).toString();
    }

    private HashMap<String, Object> buildJsonFromEditText(EditText editText) {
        HashMap<String, Object> object = new HashMap<String, Object>();

        object.put("text", editText.getText().toString().trim());
        object.put("fontsize", editText.getTextSize());

        return object;
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
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.send_to))
                    .setItems(R.array.facebook_or_contacts, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                ensureUserLoggedInToFacebookThenSendMessage();
                            } else if (which == 1) {
                                getEmailAddressFromContacts();
                            }
                        }


                    })
                    .create()
                    .show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ensureUserLoggedInToFacebookThenSendMessage() {
        if (ParseFacebookUtils.isLinked(ParseUser.getCurrentUser())) {
            mProgressDialog = ProgressDialog.show(this, "", getString(R.string.logging_in), true);

            List<String> permissions = Arrays.asList("email", "user_friends");
            ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException err) {
                    mProgressDialog.dismiss();

                    if (user != null) {
                        sendMessageViaFacebook();
                    }
                }
            });
        } else {
            sendMessageViaFacebook();
        }
    }

    private void sendMessageViaFacebook() {
        Session.openActiveSession(this, true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                startActivityForResult(new Intent(SendCatMessageActivity.this, FacebookFriendPicker.class), PICK_FB_CONTACT);
            }
        });
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
