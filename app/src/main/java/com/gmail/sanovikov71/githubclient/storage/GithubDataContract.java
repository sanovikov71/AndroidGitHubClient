package com.gmail.sanovikov71.githubclient.storage;

import android.net.Uri;
import android.provider.BaseColumns;

public class GithubDataContract {

    public static final String AUTHORITY = "com.gmail.sanovikov71";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_USERS = "users";

    public static final class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USERS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + AUTHORITY + "/" + PATH_USERS;
        public static final String CONTENT_TYPE_ITEM =
                "vnd.android.cursor.dir/" + AUTHORITY + "/" + PATH_USERS + "/*";

        // Table name
        public static final String TABLE_NAME = "users";

        // Table columns
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_LOGIN = "login";
        public static final String COLUMN_AVATAR_URL = "avatar_url";

    }
}
