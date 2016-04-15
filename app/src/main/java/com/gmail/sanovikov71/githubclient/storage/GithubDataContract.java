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
        public static final String COLUMN_GITHUB_ID = "github_id";
        public static final String COLUMN_LOGIN = "login";
        public static final String COLUMN_AVATAR_URL = "avatar_url";

        public static Uri buildUserUri(int id) {
            return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
        }

        public static Uri buildUserRepoUri() {
            return Uri.withAppendedPath(CONTENT_URI, "1/1");
        }
    }

    public static final String PATH_REPOS = "repos";

    public static final class RepoEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPOS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + AUTHORITY + "/" + PATH_REPOS;
        public static final String CONTENT_TYPE_ITEM =
                "vnd.android.cursor.dir/" + AUTHORITY + "/" + PATH_REPOS + "/*";

        // Table name
        public static final String TABLE_NAME = "repos";

        // Table columns
        public static final String COLUMN_GITHUB_ID = "github_id";
        public static final String COLUMN_OWNER_ID = "owner_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SIZE = "size";

    }
}
