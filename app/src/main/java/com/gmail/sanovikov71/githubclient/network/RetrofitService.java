
package com.gmail.sanovikov71.githubclient.network;

import com.gmail.sanovikov71.githubclient.model.Repo;
import com.gmail.sanovikov71.githubclient.model.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

// TODO: Rename it!
public interface RetrofitService {

    @GET("users")
    Call<List<User>> fetchUsers(@Query("since") int since);

    @GET("users/{login}")
    Call<User> fetchUser(@Path("login") String login);

    @GET("users/{owner}/repos")
    Call<List<Repo>> fetchRepos(@Path("owner") String owner);

}
