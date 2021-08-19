package com.optic.tourimsapp.activities.GuiaTuristicos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.GeofireProvider;
import com.optic.tourimsapp.providers.TuristaBookingProvider;

public class NotificationBookingActivity extends AppCompatActivity {

    private TextView txtViewDestino;
    private TextView txtViewOrigen;
    private TextView txtViewMin;
    private TextView txtViewDistancia;
    private TextView txtViewContador;
    private Button btnAceptar;
    private Button btnCancelar;

    private TuristaBookingProvider turistaBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;


    private String mExtraIdTurista;
    private String mExtraOrigen;
    private String mExtraDestino;
    private String mExtraMin;
    private String mExtraDistancia;

    private int mContador = 10;

    private ValueEventListener mListener;

    private Handler mHandler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mContador = mContador - 1;
            txtViewContador.setText(String.valueOf(mContador));
            if(mContador > 0){
                inicializarTemporizador();
            }else{
                cancelarBooking();
            }
        }
    };


    private void inicializarTemporizador() {
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 1000);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);

        txtViewDestino = findViewById(R.id.txtViewDestino);
        txtViewOrigen = findViewById(R.id.txtViewOrigen);
        txtViewMin = findViewById(R.id.txtViewMin);
        txtViewDistancia = findViewById(R.id.txtViewDistance);
        txtViewContador = findViewById(R.id.txtViewCounter);

        btnAceptar = findViewById(R.id.btnAceptarBooking);
        btnCancelar = findViewById(R.id.btnCancelarBooking);

        mExtraIdTurista = getIntent().getStringExtra("idTurista");
        mExtraOrigen = getIntent().getStringExtra("origen");
        mExtraDestino = getIntent().getStringExtra("destino");
        mExtraMin = getIntent().getStringExtra("tiempo");
        mExtraDistancia = getIntent().getStringExtra("distancia");

        txtViewDestino.setText(mExtraDestino);
        txtViewOrigen.setText(mExtraOrigen);
        txtViewMin.setText(mExtraMin);
        txtViewDistancia.setText(mExtraDistancia);

        turistaBookingProvider = new TuristaBookingProvider();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        inicializarTemporizador();
        verificarSiTuristaCanceloSolicitud();

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aceptarBooking();
            }
        });
        
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelarBooking();
            }
        });
    }

    private void verificarSiTuristaCanceloSolicitud(){
        mListener = turistaBookingProvider.obtenerTuristaBooking(mExtraIdTurista).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mExtraIdTurista != null){
                    if(!snapshot.exists()){
                        Toast.makeText(NotificationBookingActivity.this,"El turista CANCELO la solicitud", Toast.LENGTH_LONG).show();
                        if(mHandler != null) mHandler.removeCallbacks(runnable);
                        Intent intent = new Intent(NotificationBookingActivity.this,MapGuiaTuristicoActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cancelarBooking() {

        if(mHandler != null) mHandler.removeCallbacks(runnable);
        //turistaBookingProvider = new TuristaBookingProvider();
        turistaBookingProvider.updateStatus(mExtraIdTurista,"cancelado");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);//El id que se encuentra en el metodo de las notificaciones de la clase MyFirebaseMessagingClient
        Intent intent = new Intent(NotificationBookingActivity.this,MapGuiaTuristicoActivity.class);
        startActivity(intent);
        finish();
    }

    private void aceptarBooking() {
        if(mHandler != null) mHandler.removeCallbacks(runnable);
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("guias_turisticos_activos");
        mGeofireProvider.eliminarLocalizacion(mAuthProvider.getId());
        turistaBookingProvider = new TuristaBookingProvider();
        turistaBookingProvider.updateStatus(mExtraIdTurista,"aceptado");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);//El id que se encuentra en el metodo de las notificaciones de la clase MyFirebaseMessagingClient
        Intent intent1 = new Intent(NotificationBookingActivity.this, MapGuiaBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idTurista", mExtraIdTurista);
        startActivity(intent1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler != null) mHandler.removeCallbacks(runnable);
        if(mListener != null){//Con el fin de dejar de escuchar los cambios de la BD
            turistaBookingProvider.obtenerTuristaBooking(mExtraIdTurista).removeEventListener(mListener);
        }
    }
}