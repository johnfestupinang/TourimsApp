package com.optic.tourimsapp.retrofit;

import com.optic.tourimsapp.modelos.FCMBody;
import com.optic.tourimsapp.modelos.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

//FCM -> Firebase Cloud Messaging
//INTERFAZ PARA ENVIAR NOTIFICACIONES DE DISPOSITIVO A DISPOSITIVO
public interface IFCMApu {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAB8MoMh8:APA91bFzawg62yOkVrsFDQkpKc-x86s-ggbztaKtqjcp6m_PMEvSYES2-nyMm-RbwHtiPYftjF3kFx46uhNMuQ42DZct9QTR_HWFbUrZVDdtiS_0-QcPfPXzzZQY6d4BQdhWm43P-UYK"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
