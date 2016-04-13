package com.gmail.sanovikov71.githubclient.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.RepoEntry;

class GithubDataHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 8;

    private static final String DATABASE_NAME = "cards.db";

    public GithubDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserEntry.COLUMN_LOGIN + " TEXT NOT NULL, " +
                UserEntry.COLUMN_AVATAR_URL + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);

        final String SQL_CREATE_REPO_TABLE = "CREATE TABLE " + RepoEntry.TABLE_NAME + " (" +
                RepoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RepoEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                RepoEntry.COLUMN_SIZE + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_REPO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

}
