package com.optic.tourimsapp.activities.GuiaTuristicos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.Turistas.HistoryBookingTuristaActivity;
import com.optic.tourimsapp.adapters.HistoryBookingGuiaAdapter;
import com.optic.tourimsapp.adapters.HistoryBookingTuristaAdapter;
import com.optic.tourimsapp.includes.MyToolbar;
import com.optic.tourimsapp.modelos.HistorialBooking;
import com.optic.tourimsapp.providers.AuthProvider;

public class HistoryBookingGuiaActivity extends AppCompatActivity {

    private RecyclerView mRecycleView;
    private HistoryBookingGuiaAdapter mAdapter;
    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_guia);

        MyToolbar.show(this,"Historial de Recorridos",true);

        mRecycleView = findViewById(R.id.recycleViewHistoryBooking);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(linearLayoutManager);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthProvider = new AuthProvider();
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("TuristaBooking")//HistoryBooking
                .orderByChild("idGuiaTuristico")
                .equalTo(mAuthProvider.getId());
        FirebaseRecyclerOptions<HistorialBooking> options = new FirebaseRecyclerOptions.Builder<HistorialBooking>()
                .setQuery(query,HistorialBooking.class)
                .build();
        mAdapter = new HistoryBookingGuiaAdapter(options, HistoryBookingGuiaActivity.this);

        mRecycleView.setAdapter(mAdapter);
        mAdapter.startListening();


    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}