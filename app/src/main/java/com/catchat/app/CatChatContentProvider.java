package com.catchat.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class CatChatContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.catchat.app.CatChatContentProvider";
    public static final String SCHEME = "content";
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

    private static final UriMatcher uriMatcher;

    private static final int MESSAGE_BY_CONTACTS = 1;
    private static final int MESSAGE = 2;
    private static final int MESSAGES_SENT = 3;

    private static final String MESSAGES_SENT_TABLE_NAME = "Messages_Sent";
    private static final String MESSAGES_BY_FROM_USER_PARSE_ID = "messages_group_by_from_user";
    private static final String MESSAGES = "messages_table";

    public static final Uri MESSAGES_TABLE_GROUP_BY_FROM_USER_ID_URI = Uri.withAppendedPath(CONTENT_URI, MESSAGES_BY_FROM_USER_PARSE_ID);
    public static final Uri MESSAGES_TABLE_MESSAGES = Uri.withAppendedPath(CONTENT_URI, MESSAGES);
    public static final Uri SENT_MESSAGES = Uri.withAppendedPath(CONTENT_URI, MESSAGES_SENT_TABLE_NAME);

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, MESSAGES_BY_FROM_USER_PARSE_ID, MESSAGE_BY_CONTACTS);
        uriMatcher.addURI(AUTHORITY, MESSAGES, MESSAGE);
        uriMatcher.addURI(AUTHORITY, MESSAGES_SENT_TABLE_NAME, MESSAGES_SENT);
    }

    private CatChatDatabase mCatChatDatabase;

    private static final String MESSAGES_TABLE_NAME = "Messages";
    public static final String ID = "_id";
    public static final String MESSAGE_PARSE_ID = "Message_ID";
    public static final String MESSAGE_IMAGE_ID = "Message_Image_Id";
    public static final String MESSAGE_FROM_USER_EMAIL = "From_Email";
    public static final String MESSAGE_FROM_USER_PARSE_ID = "From_Id";
    public static final String MESSAGE_SENT_TIME = "Message_Sent";
    public static final String MESSAGE_CONTENTS = "Message_Contents";

    @Override
    public boolean onCreate() {
        mCatChatDatabase = new CatChatDatabase(getContext());

        return true;
    }

    public CatChatContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MESSAGE:
                return MESSAGES_TABLE_NAME;
            case MESSAGE_BY_CONTACTS:
                return MESSAGES_TABLE_NAME;
            case MESSAGES_SENT:
                return MESSAGES_SENT_TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long insertedId = -1;
        boolean messageAlreadyInsertedException = false;
        try {
            SQLiteDatabase db = mCatChatDatabase.getWritableDatabase();
            insertedId = db.insertOrThrow(getType(uri), null, values);
        } catch (Exception e) {
            if(e.getMessage().contains("column " + MESSAGE_PARSE_ID + " is not unique")) {
                messageAlreadyInsertedException = true;
            } else {
                Log.e("CatChatProvider", "Exception whilst inserting", e);
            }
        }

        // return success if inserted or its already there
        if (-1 != insertedId || messageAlreadyInsertedException) {
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.withAppendedPath(uri, Long.toString(1));
        } else {
            Log.d("CatChatProvider", "Failed to insert a Message");
            return uri;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case MESSAGE_BY_CONTACTS:
                return runQuery(uri, columns, selection, selectionArgs, MESSAGE_FROM_USER_PARSE_ID, sortOrder);
            default:
                return runQuery(uri, columns, selection, selectionArgs, null, sortOrder);
        }
    }

    private Cursor runQuery(Uri uri, String[] columns, String selection, String[] selectionArgs, String groupBy, String sortOrder) {
        SQLiteDatabase db = mCatChatDatabase.getReadableDatabase();
        Cursor cursor = db.query(getType(uri), columns, selection, selectionArgs, groupBy, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    class CatChatDatabase extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "CatChatDb";

        public CatChatDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " + MESSAGES_TABLE_NAME +
                        " (" + ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        MESSAGE_PARSE_ID + " NVARCHAR(16) UNIQUE NOT NULL," +
                        MESSAGE_IMAGE_ID + " NVARCHAR(16) NOT NULL," +
                        MESSAGE_FROM_USER_PARSE_ID + " NVARCHAR(16) NOT NULL," +
                        MESSAGE_FROM_USER_EMAIL + " NVARCHAR(32) NOT NULL," +
                        MESSAGE_CONTENTS + " NVARCHAR NOT NULL," +
                        MESSAGE_SENT_TIME + " NVARCHAR(50) NOT NULL" + ");");

                db.execSQL("CREATE TABLE " + MESSAGES_SENT_TABLE_NAME +
                        " (" + ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        MESSAGE_IMAGE_ID + " NVARCHAR(16) NOT NULL," +
                        MESSAGE_CONTENTS + " NVARCHAR NOT NULL," +
                        MESSAGE_SENT_TIME + " NVARCHAR(50) NOT NULL" + ");");
            } catch (Exception e) {
                Log.e("CatChatProvider", e.getClass().toString(), e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
