package com.optic.tourimsapp.providers;

import com.optic.tourimsapp.modelos.FCMBody;
import com.optic.tourimsapp.modelos.FCMResponse;
import com.optic.tourimsapp.retrofit.IFCMApu;
import com.optic.tourimsapp.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApu.class).send(body);
    }
}
