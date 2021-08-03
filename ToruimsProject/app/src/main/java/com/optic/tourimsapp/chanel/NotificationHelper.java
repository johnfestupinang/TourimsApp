package com.optic.tourimsapp.chanel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.optic.tourimsapp.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.optic.toirimsapp";
    private static final String CHANNEL_NAME = "TourimsApp";

    private NotificationManager manager;


    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.O){//DISPOSITIVO MOVIL OREO O SUPERIOR
                createChannels();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels(){
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager (){
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return manager;
    }

    //PARA VERSIONES SUPERIORES A ANDROID OREO
    @RequiresApi(api = Build.VERSION_CODES.O)
    public  Notification.Builder getNotification (String titulo, String contenido, PendingIntent intent , Uri soundUri){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_tourims)
                .setStyle(new Notification.BigTextStyle().bigText(contenido).setBigContentTitle(titulo));
    }

    //PARA VERSIONES INFERIORES A ANDROID OREO
    public NotificationCompat.Builder getNotificationOldAPI (String titulo, String contenido, PendingIntent intent , Uri soundUri){
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_tourims)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contenido).setBigContentTitle(titulo));
    }

    //PARA VERSIONES SUPERIORES A ANDROID OREO
    @RequiresApi(api = Build.VERSION_CODES.O)
    public  Notification.Builder getNotificationActions (String titulo, String contenido,
                                                         Uri soundUri, Notification.Action aceptarAccion,
                                                        Notification.Action cancelarAccion){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(soundUri)
                .addAction(aceptarAccion)
                .addAction(cancelarAccion)
                .setSmallIcon(R.drawable.ic_tourims)
                .setStyle(new Notification.BigTextStyle().bigText(contenido).setBigContentTitle(titulo));
    }

    //PARA VERSIONES INFERIORES A ANDROID OREO
    public NotificationCompat.Builder getNotificationOldAPIActions (String titulo, String contenido,
                                                                    Uri soundUri, NotificationCompat.Action aceptarAccion,
                                                                    NotificationCompat.Action cancelarAccion){
        return new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setAutoCancel(true)
                .setSound(soundUri)
                .addAction(aceptarAccion)
                .addAction(cancelarAccion)
                .setSmallIcon(R.drawable.ic_tourims)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contenido).setBigContentTitle(titulo));
    }


}
