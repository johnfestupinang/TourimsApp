package com.optic.tourimsapp.activities.GuiaTuristicos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.Turistas.HistoryBookingDetailTuristaActivity;
import com.optic.tourimsapp.modelos.HistorialBooking;
import com.optic.tourimsapp.providers.GuiaTuristicoProvider;
import com.optic.tourimsapp.providers.HistorialBookingProvider;
import com.optic.tourimsapp.providers.TuristaProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetailGuiaActivity extends AppCompatActivity {

    private TextView txtViewNombre;
    private TextView txtViewOrigen;
    private TextView txtViewDestino;
    private TextView txtViewTuCalificacion;
    private RatingBar mRatingBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageBack;
    private String mExtraId;

    private HistorialBookingProvider mHistoryBookingProvider;
    private TuristaProvider mTuristaProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_guia);

        txtViewNombre = findViewById(R.id.txtViewNameBookingDetail);
        txtViewOrigen = findViewById(R.id.txtViewOriginHistoryBookingDetail);
        txtViewDestino = findViewById(R.id.txtViewDestinationHistoryBookingDetail);
        txtViewTuCalificacion = findViewById(R.id.txtViewCalificationHistoryBookingDetail);
        mRatingBarCalification = findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage = findViewById(R.id.circleImageHistoryBookingDetail);
        mCircleImageBack = findViewById(R.id.circleImageBack);

        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider = new HistorialBookingProvider();
        mTuristaProvider = new TuristaProvider();
        obtenerHistoryBookin();

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




    }

    private void obtenerHistoryBookin() {
        mHistoryBookingProvider.obtenerHisotrialBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //Obtener dara de forma rapida
                    HistorialBooking historyBooking = snapshot.getValue(HistorialBooking.class);
                    txtViewOrigen.setText(historyBooking.getOrigen());
                    txtViewDestino.setText(historyBooking.getDestino());
                    txtViewTuCalificacion.setText("Tu calificacion: "+historyBooking.getCalificacionGuia());
                    if(snapshot.hasChild("calificacionTurista")){
                        mRatingBarCalification.setRating((float) historyBooking.getCalificacionTurista());
                    }
                    mTuristaProvider.obtenerCliente(historyBooking.getIdTurista()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String nombreCompleto = snapshot.child("nombreCompleto").getValue().toString();
                                txtViewNombre.setText(nombreCompleto.toUpperCase());
                                if(snapshot.hasChild("Imagen")){
                                    String imagen = snapshot.child("Imagen").getValue().toString();
                                    Picasso.with(HistoryBookingDetailGuiaActivity.this)
                                            .load(imagen).into(mCircleImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}