
package com.gmail.sanovikov71.githubclient.storage;

import static com.gmail.sanovikov71.githubclient.storage.GithubDataContract.AUTHORITY;
import static com.gmail.sanovikov71.githubclient.storage.GithubDataContract.PATH_REPOS;
import static com.gmail.sanovikov71.githubclient.storage.GithubDataContract.PATH_USERS;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.RepoEntry;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;

public class GithubDataLocalStorage extends ContentProvider {

    private static final int USER_LIST = 100;
    private static final int USER_ID = 101;
    private static final int REPO_LIST = 200;
    private static final int REPO_ID = 201;
    private static final UriMatcher URI_MATCHER;

    private GithubDataHelper mHelper;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, PATH_USERS, USER_LIST);
        URI_MATCHER.addURI(AUTHORITY, PATH_USERS + "/#", USER_ID);
        URI_MATCHER.addURI(AUTHORITY, PATH_REPOS, REPO_LIST);
        URI_MATCHER.addURI(AUTHORITY, PATH_REPOS + "/#", REPO_ID);
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

    private static final String sUserTables = UserEntry.TABLE_NAME;

    private static final String sRepoTables = RepoEntry.TABLE_NAME;

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db = mHelper.getWritableDatabase();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String rowID = null;

        switch (URI_MATCHER.match(uri)) {
            case USER_ID:
                rowID = uri.getPathSegments().get(1);
                queryBuilder.setTables(sUserTables);
                queryBuilder.appendWhere(UserEntry.COLUMN_GITHUB_ID + " = " + rowID);
                break;
            case USER_LIST:
                queryBuilder.setTables(sUserTables);
                break;
            case REPO_ID:
                queryBuilder.setTables(sRepoTables);
                rowID = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(RepoEntry.COLUMN_GITHUB_ID + " = " + rowID);
                break;
            case REPO_LIST:
                queryBuilder.setTables(sRepoTables);
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

        switch (URI_MATCHER.match(uri)) {
            case USER_LIST:
                id = db.insertWithOnConflict(UserEntry.TABLE_NAME, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                insertedId = ContentUris.withAppendedId(UserEntry.CONTENT_URI, id);
                break;
            case REPO_LIST:
                id = db.insertWithOnConflict(RepoEntry.TABLE_NAME, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
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
        throw new RuntimeException("Method delete is not implemented for now");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("Method update is not implemented for now");
    }

}
