package com.catchat.app.ui;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.catchat.app.data.CatChatContentProvider;
import com.catchat.app.R;
import com.catchat.app.Utils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ConversationActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private String mUserId;
    private boolean mShowSentMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation);

        Bundle extras = getIntent().getExtras();

        mShowSentMessages = extras.getBoolean("sentMessages", false);

        if(mShowSentMessages) {
            getActionBar().setTitle(R.string.sent_messages);
        } else {
            mUserId = extras.getString("fromUserId");
            String userEmail = extras.getString("fromUserEmail");

            getActionBar().setTitle(userEmail);
        }

        final ListAdapter adapter = new ConversationAdapter(this, null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        getListView().setAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(mShowSentMessages) {
            return new CursorLoader(this,
                    CatChatContentProvider.SENT_MESSAGES,
                    new String[]{CatChatContentProvider.MESSAGE_CONTENTS, CatChatContentProvider.ID, CatChatContentProvider.MESSAGE_IMAGE_ID, CatChatContentProvider.MESSAGE_SENT_TIME},
                    null,
                    null,
                    CatChatContentProvider.MESSAGE_SENT_TIME);
        }
        return new CursorLoader(this,
                CatChatContentProvider.MESSAGES_TABLE_MESSAGES,
                new String[]{CatChatContentProvider.MESSAGE_CONTENTS, CatChatContentProvider.ID, CatChatContentProvider.MESSAGE_IMAGE_ID, CatChatContentProvider.MESSAGE_SENT_TIME},
                CatChatContentProvider.MESSAGE_FROM_USER_PARSE_ID + "=?",
                new String[]{mUserId},
                CatChatContentProvider.MESSAGE_SENT_TIME);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor c) {
        ((CursorAdapter) getListView().getAdapter()).swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        ((CursorAdapter) getListView().getAdapter()).swapCursor(null);
    }

    private class ConversationAdapter extends CursorAdapter {
        private final LayoutInflater mLayoutInflater;

        public ConversationAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);

            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return mLayoutInflater.inflate(R.layout.message_row, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String imageId = cursor.getString(cursor.getColumnIndex(CatChatContentProvider.MESSAGE_IMAGE_ID));
            String contents = cursor.getString(cursor.getColumnIndex(CatChatContentProvider.MESSAGE_CONTENTS));
            String sentTime = cursor.getString(cursor.getColumnIndex(CatChatContentProvider.MESSAGE_SENT_TIME));

            setCatImage(view, imageId);
            setTopAndBottomCaptions(view, contents);
            setTimestamp(view, sentTime);
        }

        private void setTimestamp(View view, String sentTime) {
            TextView textView = (TextView)view.findViewById(R.id.timestamp_textview);

            textView.setText(Utils.getDisplayDate(sentTime));
        }

        private void setTopAndBottomCaptions(View view, String contents) {

            try {
                JSONObject jsonObject = new JSONObject(contents);

                parseJsonAndSetText(view, jsonObject.getJSONObject("top"), R.id.top_textview);
                parseJsonAndSetText(view, jsonObject.getJSONObject("bottom"), R.id.bottom_textview);
            } catch (JSONException e) {
                Log.e("CatChatTag", "Error parsing message JSON", e);
            }
        }

        private void setCatImage(View view, String imageId) {
            final ImageView imageView = (ImageView) view.findViewById(R.id.imageview);

            // TODO: request elsewhere
            ParseQuery<ParseObject> query = ParseQuery.getQuery("CatImage");
            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            query.whereEqualTo("objectId", imageId);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (parseObjects != null && parseObjects.size() > 0) {

                        try {
                            ParseFile file = parseObjects.get(0).getParseFile("image");
                            Bitmap b = BitmapFactory.decodeByteArray(file.getData(), 0, file.getData().length);

                            imageView.setImageBitmap(b);

                        } catch (ParseException ex) {
                            Log.d("CatChatTag", "Failed to decode bitmap", ex);
                        }
                    } else {
                        Log.e("CatChatTag", "Error pulling cat pic", e);
                    }
                }
            });
        }

        private void parseJsonAndSetText(View view, JSONObject jsonObject, int id) throws JSONException {
            String text = jsonObject.getString("text");

            if (!TextUtils.isEmpty(text)) {
                TextView top = (TextView) view.findViewById(id);
                top.setText(text);
            }
        }
    }
}
