package com.optic.tourimsapp.activities.GuiaTuristicos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.Turistas.CalificacionGuiaActivity;
import com.optic.tourimsapp.activities.Turistas.MapTuristaActivity;
import com.optic.tourimsapp.modelos.HistorialBooking;
import com.optic.tourimsapp.modelos.TuristaBooking;
import com.optic.tourimsapp.providers.HistorialBookingProvider;
import com.optic.tourimsapp.providers.TuristaBookingProvider;

import java.util.Date;

public class CalificacionTuristaActivity extends AppCompatActivity {

    private TextView txtViewOrigin;
    private TextView txtViewdestination;
    private RatingBar mRatingBar;
    private Button btnCalificacion;

    private TuristaBookingProvider mTuristaBookingProvider;

    private String mExtraClientId ;

    private HistorialBooking mHistorialBooking;
    private HistorialBookingProvider mHistorialBookingProvider;

    private float mCalificacion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calificacion_turista);

        txtViewOrigin = findViewById(R.id.txtViewOrigenCalificacion);
        txtViewdestination = findViewById(R.id.txtViewDestinoCalificacion);
        mRatingBar = findViewById(R.id.ratingBarCalificacion);
        btnCalificacion = findViewById(R.id.btnCalificacion);


        mTuristaBookingProvider = new TuristaBookingProvider();
        mHistorialBookingProvider = new HistorialBookingProvider();

        mExtraClientId = getIntent().getStringExtra("idTurista");

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float calificacion, boolean fromUser) {
                mCalificacion = calificacion;
            }
        });

        btnCalificacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calificacion();
            }
        });

        obtenerTuristaBooking();

    }

    private void obtenerTuristaBooking(){
        mTuristaBookingProvider.obtenerTuristaBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    TuristaBooking  turistaBooking = snapshot.getValue(TuristaBooking.class);
                    txtViewOrigin.setText(turistaBooking.getOrigen());
                    txtViewdestination.setText(turistaBooking.getDestino());
                    mHistorialBooking = new HistorialBooking(
                        turistaBooking.getIdHistoryBooking(),
                            turistaBooking.getIdTurista(),
                            turistaBooking.getIdGuiaTuristico(),
                            turistaBooking.getDestino(),
                            turistaBooking.getOrigen(),
                            turistaBooking.getTiempo(),
                            turistaBooking.getDistancia(),
                            turistaBooking.getEstado(),
                            turistaBooking.getOrigenLat(),
                            turistaBooking.getOrigenLng(),
                            turistaBooking.getDestinoLat(),
                            turistaBooking.getDestinoLng()
                    );

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calificacion() {
        if(mCalificacion > 0){
            mHistorialBooking.setCalificacionTurista(mCalificacion);
            mHistorialBooking.setTimestamp(new Date().getTime());
            mHistorialBookingProvider.obtenerHisotrialBooking(mHistorialBooking.getIdHistorialBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        mHistorialBookingProvider.actualizarCalificacionTurista(mCalificacion,mHistorialBooking.getIdHistorialBooking()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(CalificacionTuristaActivity.this, "Calificación guardad correctamente",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CalificacionTuristaActivity.this, MapGuiaTuristicoActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                    }else{
                        mHistorialBookingProvider.create(mHistorialBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(CalificacionTuristaActivity.this, "Calificación guardad correctamente",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CalificacionTuristaActivity.this, MapGuiaTuristicoActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });




        }else{
            Toast.makeText(this, "Debe ingresar una calificación",Toast.LENGTH_LONG).show();
        }

    }
}