
package com.gmail.sanovikov71.githubclient.storage;

import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.RepoEntry;

public class DBConstants {

    public static final String[] USER_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry._ID,
            UserEntry.COLUMN_LOGIN,
            UserEntry.COLUMN_AVATAR_URL
    };

    public static final String[] REPO_COLUMNS = {
            RepoEntry.TABLE_NAME + "." + RepoEntry._ID,
            RepoEntry.COLUMN_NAME,
            RepoEntry.COLUMN_SIZE
    };



}
