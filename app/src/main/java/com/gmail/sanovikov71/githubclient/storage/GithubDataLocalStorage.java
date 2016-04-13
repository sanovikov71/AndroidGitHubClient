
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

import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.RepoEntry;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;

public class GithubDataLocalStorage extends ContentProvider {

    public static final String TAG = GithubDataLocalStorage.class.getSimpleName();

    private static final int USER_LIST = 1;
    private static final int USER_ID = 2;
    private static final int REPO_LIST = 3;
    private static final int REPO_ID = 4;
    private static final UriMatcher URI_MATCHER;

    private GithubDataHelper mHelper;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(GithubDataContract.AUTHORITY, GithubDataContract.PATH_USERS, USER_LIST);
        URI_MATCHER.addURI(GithubDataContract.AUTHORITY, GithubDataContract.PATH_USERS + "/#", USER_ID);
        URI_MATCHER.addURI(GithubDataContract.AUTHORITY, GithubDataContract.PATH_REPOS, REPO_LIST);
        URI_MATCHER.addURI(GithubDataContract.AUTHORITY, GithubDataContract.PATH_REPOS + "/#", REPO_ID);
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
            case REPO_LIST:
                return RepoEntry.CONTENT_TYPE;
            case REPO_ID:
                return RepoEntry.CONTENT_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        Log.i(TAG, "path: " + uri.getPathSegments());

        SQLiteDatabase db = mHelper.getWritableDatabase();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String rowID = null;

        switch (URI_MATCHER.match(uri)) {
            case USER_ID:
                rowID = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(UserEntry.COLUMN_ID + " = " + rowID);
                queryBuilder.setTables(UserEntry.TABLE_NAME);
                break;
            case USER_LIST:
                queryBuilder.setTables(UserEntry.TABLE_NAME);
                break;
            case REPO_ID:
                rowID = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(RepoEntry.COLUMN_ID + " = " + rowID);
                queryBuilder.setTables(RepoEntry.TABLE_NAME);
                break;
            case REPO_LIST:
                queryBuilder.setTables(RepoEntry.TABLE_NAME);
                break;
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

        long id = -1;
        Uri insertedId = null;

        Log.i(TAG, "provider insert");
        Log.i(TAG, "uri: " + uri);

        switch (URI_MATCHER.match(uri)) {
            case USER_LIST:
                Log.i(TAG, "provider insert USER_ID");
                id = db.insert(UserEntry.TABLE_NAME, null, values);
                insertedId = ContentUris.withAppendedId(UserEntry.CONTENT_URI, id);
                break;
            case REPO_LIST:
                Log.i(TAG, "provider insert REPO_ID");
                id = db.insert(RepoEntry.TABLE_NAME, null, values);
                insertedId = ContentUris.withAppendedId(RepoEntry.CONTENT_URI, id);
                break;
            default:
                break;
        }

        if (id > -1) {
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
                break;
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
