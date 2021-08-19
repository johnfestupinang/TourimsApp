package com.optic.tourimsapp.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.GuiaTuristicos.NotificationBookingActivity;
import com.optic.tourimsapp.chanel.NotificationHelper;
import com.optic.tourimsapp.receivers.AcceptReceiver;
import com.optic.tourimsapp.receivers.CancelReceiver;

import java.util.Map;

public class MyFirebaseMessagingClient extends FirebaseMessagingService {

    private static final int NOTIFICATION_CODE = 100;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");

        if(title != null){
            //String idTurista = data.get("idTurista");
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                if(title.contains("SOLICITUD DE GUIA TURISTICO")){
                    //showNotificationAPIOreo(title,body);//PARA VERSIONES RECIENTES
                    String idTurista = data.get("idTurista");
                    String origen = data.get("origen");
                    String destino = data.get("destino");
                    String tiempo = data.get("tiempo");
                    String distancia = data.get("distancia");
                    showNotificationAPIOreoActions(title,body, idTurista);
                    shownotificationActivity(idTurista,origen,destino,tiempo,distancia);
                }else if(title.contains("SOLICITUD CANCELADA")){
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);//El id que se encuentra en el metodo de las notificaciones de la clase MyFirebaseMessagingClient
                    showNotificationAPIOreo(title,body);
                }else{
                    showNotificationAPIOreo(title,body);//PARA VERSIONES RECIENTES
                }

            }else{
                if(title.contains("SOLICITUD DE GUIA TURISTICO")){
                    String idTurista = data.get("idTurista");
                    String origen = data.get("origen");
                    String destino = data.get("destino");
                    String tiempo = data.get("tiempo");
                    String distancia = data.get("distancia");
                    showNotificationActions(title,body,idTurista);
                    shownotificationActivity(idTurista,origen,destino,tiempo,distancia);
                    //showNotification(title,body);//VERSIONES ANTIGUAS
                }else if(title.contains("SOLICITUD CANCELADA")){
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);//El id que se encuentra en el metodo de las notificaciones de la clase MyFirebaseMessagingClient
                    showNotification(title,body);
                }else{
                    showNotification(title,body);//VERSIONES ANTIGUAS
                }

            }
        }
    }

    private void shownotificationActivity(String idTurista, String origen, String destino, String tiempo, String distancia) {
        PowerManager pm = (PowerManager)getBaseContext().getSystemService(Context.POWER_SERVICE); //PARA ENCENDER LA PANTALLA CUANDO ESTE BOQUEADA
        boolean pantallaEstaEncendida = pm.isScreenOn();
        //boolean pantallaEstaEncendida;
        /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            pantallaEstaEncendida = pm.isInteractive();
        }else{
            pantallaEstaEncendida = pm.isScreenOn();
        }*/

        if(!pantallaEstaEncendida){
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                    "AppName:MyLock"
            );
            wakeLock.acquire(10000);//MILISEGUNDOS
        }
        Intent intent = new Intent(getBaseContext(), NotificationBookingActivity.class);
        intent.putExtra("idTurista",idTurista);
        intent.putExtra("origen",origen);
        intent.putExtra("destino",destino);
        intent.putExtra("tiempo",tiempo);
        intent.putExtra("distancia",distancia);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    private void showNotification(String titulo, String contenido){
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPI(titulo,contenido, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    private void showNotificationActions(String titulo, String contenido, String idTurista){

        //Aceptar
        Intent acceptIntent = new Intent(MyFirebaseMessagingClient.this, AcceptReceiver.class);
        acceptIntent.putExtra("idTurista",idTurista);
        PendingIntent accetPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE,acceptIntent,PendingIntent.FLAG_UPDATE_CURRENT );
        NotificationCompat.Action aceptarAccion = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                accetPendingIntent
        ).build();

        //Cancelar
        Intent cancelarIntent = new Intent(MyFirebaseMessagingClient.this, CancelReceiver.class);
        cancelarIntent.putExtra("idTurista",idTurista);
        PendingIntent cancelarPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE,cancelarIntent,PendingIntent.FLAG_UPDATE_CURRENT );
        NotificationCompat.Action cancelarAccion = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelarPendingIntent
        ).build();

        //PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        NotificationCompat.Builder builder = notificationHelper.getNotificationOldAPIActions(titulo,contenido, sound,aceptarAccion,cancelarAccion);
        notificationHelper.getManager().notify(2, builder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationAPIOreo(String titulo, String contenido) {
        PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getNotification(titulo,contenido, intent, sound);
        notificationHelper.getManager().notify(1, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotificationAPIOreoActions(String titulo, String contenido, String idTurista) {

        //Aceptar
        Intent acceptIntent = new Intent(MyFirebaseMessagingClient.this, AcceptReceiver.class);
        acceptIntent.putExtra("idTurista",idTurista);
        PendingIntent accetPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE,acceptIntent,PendingIntent.FLAG_UPDATE_CURRENT );
        Notification.Action aceptarAccion = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                accetPendingIntent
        ).build();

        //Cancelar
        Intent cancelIntent = new Intent(MyFirebaseMessagingClient.this, CancelReceiver.class);
        acceptIntent.putExtra("idTurista",idTurista);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE,cancelIntent,PendingIntent.FLAG_UPDATE_CURRENT );
        Notification.Action cancelarAccion = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build();


        //PendingIntent intent = PendingIntent.getActivity(getBaseContext(), 0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelper notificationHelper = new NotificationHelper(getBaseContext());
        //Notification.Builder builder = notificationHelper.getNotification(titulo,contenido, intent, sound);
        Notification.Builder builder = notificationHelper.getNotificationActions(titulo,contenido, sound, aceptarAccion,cancelarAccion);
        notificationHelper.getManager().notify(2, builder.build());
    }


}
