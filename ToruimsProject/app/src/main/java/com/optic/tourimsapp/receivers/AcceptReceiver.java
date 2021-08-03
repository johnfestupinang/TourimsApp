package com.optic.tourimsapp.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.optic.tourimsapp.activities.GuiaTuristicos.MapGuiaBookingActivity;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.GeofireProvider;
import com.optic.tourimsapp.providers.TuristaBookingProvider;

public class AcceptReceiver extends BroadcastReceiver {

    private TuristaBookingProvider turistaBookingProvider;

    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    //METODO QUE SE EJECUTA CUANDO SEDE DA CLICK EN BOTON ACEPTAR DE LA NOTIFICACION
    @Override
    public void onReceive(Context context, Intent intent) {

        mAuthProvider = new AuthProvider();

        mGeofireProvider = new GeofireProvider("guias_turisticos_activos");
        mGeofireProvider.eliminarLocalizacion(mAuthProvider.getId());


        String idTurista = intent.getExtras().getString("idTurista");
        turistaBookingProvider = new TuristaBookingProvider();
        turistaBookingProvider.updateStatus(idTurista,"aceptado");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);//El id que se encuentra en el metodo de las notificaciones de la clase MyFirebaseMessagingClient

        Intent intent1 = new Intent(context, MapGuiaBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        context.startActivity(intent1);
    }
}
