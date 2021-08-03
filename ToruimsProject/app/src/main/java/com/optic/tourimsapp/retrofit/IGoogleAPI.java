package com.optic.tourimsapp.retrofit;

import retrofit2.http.GET;
import retrofit2.http.Url;
import retrofit2.Call;

public interface IGoogleAPI {
    @GET
    Call<String> getDirections(@Url String url);
}
