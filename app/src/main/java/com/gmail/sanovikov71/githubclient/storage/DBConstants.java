
package com.gmail.sanovikov71.githubclient.storage;

import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;

public class DBConstants {

    public static final String[] USER_COLUMNS = {
            UserEntry.TABLE_NAME + "." + UserEntry._ID,
            UserEntry.COLUMN_LOGIN,
            UserEntry.COLUMN_AVATAR_URL
    };

}
