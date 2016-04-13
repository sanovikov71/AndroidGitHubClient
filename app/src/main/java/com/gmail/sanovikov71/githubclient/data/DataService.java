
package com.gmail.sanovikov71.githubclient.data;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.gmail.sanovikov71.githubclient.model.Repo;
import com.gmail.sanovikov71.githubclient.model.User;
import com.gmail.sanovikov71.githubclient.network.RetrofitService;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.RepoEntry;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;
import com.gmail.sanovikov71.githubclient.ui.UiElement;
import com.gmail.sanovikov71.githubclient.ui.drawer.search.ServerSearchListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

public class DataService extends Service {

    private static final String API_URL = "https://api.github.com/";
    private static final String TAG = "Novikov";

    private RetrofitService mGithub;

    @Override
    public void onCreate() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mGithub = retrofit.create(RetrofitService.class);
    }

    //    public static final String ACTION_FETCH = "com.gmail.sanovikov71.githubclient.ACTION_FETCH";

    //    @Override
    //    public int onStartCommand(Intent intent, int flags, int startId) {
    //        if (ACTION_FETCH.equals(intent.getAction())) {
    //            fetchUsers();
    //        }
    //
    //        return START_NOT_STICKY;
    //    }

    public void fetchUsers(final UiElement ui) {
        fetch(ui, 0);
    }

    public void fetchMoreUsers(final UiElement ui, int offset) {
        Log.i("Novikov", "fetchMoreUsers with offset: " + offset);
        fetch(ui, offset);
    }

    public void fetch(final UiElement ui, int since) {
        Log.i("Novikov", "fetch");
        mGithub.fetchUsers(since).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                ui.hideProgressDialog();

                final List<User> body = response.body();
                if (response.isSuccessful()) {
                    final int size = body.size();
                    ContentValues userList[] = new ContentValues[size];
                    for (int i = 0; i < size; i++) {
                        ContentValues userValues = new ContentValues();
                        final User user = body.get(i);
                        userValues.put(UserEntry.COLUMN_LOGIN, user.getLogin());
                        userValues.put(UserEntry.COLUMN_AVATAR_URL, user.getAvatarUrl());
                        userList[i] = userValues;
                    }

                    getContentResolver()
                            .bulkInsert(UserEntry.CONTENT_URI, userList);
                } else {
                    ui.showError(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                ui.hideProgressDialog();
                ui.showError(-1);
            }
        });
    }

    public void fetchUser(final ServerSearchListener serverSearchListener, String login) {
        mGithub.fetchUser(login).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                final User user = response.body();
                if (response.isSuccessful()) {
                    ContentValues userValues = new ContentValues();
                    userValues.put(UserEntry.COLUMN_LOGIN, user.getLogin());
                    userValues.put(UserEntry.COLUMN_AVATAR_URL, user.getAvatarUrl());

                    getContentResolver()
                            .insert(UserEntry.CONTENT_URI, userValues);

                    serverSearchListener.onSearchResult();
                } else {
                    serverSearchListener.onSearchError(response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.i(TAG, "url: " + call.request().url().toString());
                Log.i(TAG, "failure reason: " + t.toString());
                serverSearchListener.onSearchError(-1);
            }
        });
    }

    public void fetchRepos(final UiElement ui, String ownerName) {
        Log.i(TAG, "fetchRepos");
        mGithub.fetchRepos(ownerName).enqueue(new Callback<List<Repo>>() {
            @Override
            public void onResponse(Call<List<Repo>> call, Response<List<Repo>> response) {
                ui.hideProgressDialog();

                Log.i(TAG, "fetchRepos ok");

                final List<Repo> body = response.body();
                Log.i(TAG, "fetchRepos ok: " + body.size());

                if (response.isSuccessful()) {
                    final int size = body.size();
                    ContentValues reposList[] = new ContentValues[size];
                    for (int i = 0; i < size; i++) {
                        ContentValues repoValues = new ContentValues();
                        final Repo repo = body.get(i);
                        repoValues.put(RepoEntry.COLUMN_NAME, repo.getName());
                        repoValues.put(RepoEntry.COLUMN_SIZE, repo.getSize());
                        reposList[i] = repoValues;
                    }

                    getContentResolver()
                            .bulkInsert(RepoEntry.CONTENT_URI, reposList);
                } else {
                    ui.showError(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Repo>> call, Throwable t) {
                ui.hideProgressDialog();
                ui.showError(-1);
            }
        });
    }

    // Just boilerplate as always in android

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder. Because we know this service always runs in the same process as its clients, we
     * don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public DataService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
