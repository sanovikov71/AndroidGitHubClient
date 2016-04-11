
package com.gmail.sanovikov71.githubclient.storage;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;

public class GithubDataLocalStorage extends ContentProvider {

    public static final String TAG = GithubDataLocalStorage.class.getSimpleName();

    private static final int USER_LIST = 1;
    private static final int USER_ID = 2;
    private static final UriMatcher URI_MATCHER;

    private GithubDataHelper mHelper;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(GithubDataContract.AUTHORITY, GithubDataContract.PATH_USERS, USER_LIST);
        URI_MATCHER.addURI(GithubDataContract.AUTHORITY, GithubDataContract.PATH_USERS + "/#", USER_ID);
    }

    @Override
    public boolean onCreate() {
        mHelper = new GithubDataHelper(getContext());
        return false;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case USER_LIST:
                return UserEntry.CONTENT_TYPE;
            case USER_ID:
                return UserEntry.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(UserEntry.TABLE_NAME);

        switch (URI_MATCHER.match(uri)) {
            case USER_ID:
                String rowID = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(UserEntry.COLUMN_ID + " = " + rowID);
            default:
                break;
        }

        Cursor cursor = queryBuilder
                .query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        long id = db.insert(GithubDataContract.UserEntry.TABLE_NAME, null, values);
        if (id > -1) {
            Uri insertedId = ContentUris.withAppendedId(UserEntry.CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(insertedId, null);
            return insertedId;
        } else {
            return null;
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        switch (URI_MATCHER.match(uri)) {
            case USER_ID:
                String rowID = uri.getPathSegments().get(1);
                selection = GithubDataContract.UserEntry.COLUMN_ID + " = " + rowID +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
            default:
                break;
        }

        if (selection == null) {
            selection = "1";
        }

        int deleteCount = db.delete(GithubDataContract.UserEntry.TABLE_NAME, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        Log.i(TAG, "Provider update");
        Log.i(TAG, "values w: " + values.getAsString(UserEntry.COLUMN_LOGIN));
        Log.i(TAG, "values url: " + values.getAsString(UserEntry.COLUMN_AVATAR_URL));
        Log.i(TAG, "selection: " + selection);

//        SQLiteDatabase db = mHelper.getWritableDatabase();
//
//        switch (URI_MATCHER.match(uri)) {
//            case USER_ID:
//                String rowID = uri.getPathSegments().get(1);
//                selection = UserEntry.COLUMN_ID + " = " + rowID +
//                        (!TextUtils.isEmpty(selection) ? "AND (" + selection + ')' : "");
//            default:
//                break;
//        }
//
        int updateCount = 0;
//
//        // There are two basic case when update is used
//
//        Log.i(TAG, "val: " + values.getAsString(UserEntry.COLUMN_WORD));
//        Log.i(TAG, "translation: " + values.getAsString(UserEntry.COLUMN_TRANSLATION));
//        // TODO review this part of the code
//        if (values.containsKey(UserEntry.COLUMN_WORD)
//                && values.containsKey(UserEntry.COLUMN_TRANSLATION)) {
//            // The first one is typo correcting in card details,
//            // In this case word and translation are not null and
//            // SQLiteDatabase::updateWithOnConflict method is used to completely
//            // replace a row in the database
//            updateCount = db.updateWithOnConflict(
//                    UserEntry.TABLE_NAME,
//                    values,
//                    selection,
//                    selectionArgs,
//                    SQLiteDatabase.CONFLICT_IGNORE
//                    );
//        } else {
//            // The second one is img_url updating, when a new image
//            // is selected for a card, here full replacing is
//            // unnecessary and SQLiteDatabase::update method is used to change
//            // img_url column only
//            Log.i("novikov", "val: " + values.getAsString(UserEntry.COLUMN_WORD));
//            updateCount = db.update(
//                    UserEntry.TABLE_NAME,
//                    values,
//                    selection,
//                    selectionArgs
//                    );
//        }

        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

}
