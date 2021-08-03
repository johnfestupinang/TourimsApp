package com.optic.tourimsapp.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.optic.tourimsapp.providers.TuristaBookingProvider;

public class CancelReceiver extends BroadcastReceiver {
    private TuristaBookingProvider turistaBookingProvider;

    //METODO QUE SE EJECUTA CUANDO SEDE DA CLICK EN BOTON ACEPTAR DE LA NOTIFICACION
    @Override
    public void onReceive(Context context, Intent intent) {

        String idTurista = intent.getExtras().getString("idTurista");
        turistaBookingProvider = new TuristaBookingProvider();
        turistaBookingProvider.updateStatus(idTurista,"cancelado");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);//El id que se encuentra en el metodo de las notificaciones de la clase MyFirebaseMessagingClient

    }
}
