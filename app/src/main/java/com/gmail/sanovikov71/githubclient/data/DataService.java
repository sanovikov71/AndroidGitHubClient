
package com.gmail.sanovikov71.githubclient.data;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.gmail.sanovikov71.githubclient.model.User;
import com.gmail.sanovikov71.githubclient.network.RetrofitService;
import com.gmail.sanovikov71.githubclient.storage.GithubDataContract.UserEntry;
import com.gmail.sanovikov71.githubclient.ui.UiElement;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;
import java.util.Objects;

public class DataService extends Service {

    private static final String API_URL = "https://api.github.com/";

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
        System.out.println("Novikov fetch");
        Log.i("Novikov", "fetch");
        mGithub.fetchUsers(since).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                System.out.println("Novikov onResponse");
                ui.hideProgressDialog();

                final List<User> body = response.body();
                if (response.isSuccessful()) {
                    System.out.println("Novikov good");
                } else {
                    System.out.println("Novikov bad");
                    ui.showError(response.code());
                }

                final int size = body.size();
                ContentValues userList[] = new ContentValues[size];
                for (int i = 0; i < size; i++) {
                    ContentValues cardValues = new ContentValues();
                    final User user = body.get(i);
                    cardValues.put(UserEntry.COLUMN_LOGIN, user.getLogin());
                    cardValues.put(UserEntry.COLUMN_AVATAR_URL, user.getAvatarUrl());
                    userList[i] = cardValues;
                }

                getContentResolver()
                        .bulkInsert(UserEntry.CONTENT_URI, userList);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                System.out.println("Novikov onFailure");
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
