package com.optic.tourimsapp.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.tourimsapp.modelos.Turista;
import com.optic.tourimsapp.modelos.TuristaBooking;

import java.util.HashMap;
import java.util.Map;

public class TuristaBookingProvider {
    DatabaseReference mDatabase;

    public TuristaBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TuristaBooking");
    }

    public Task<Void> create(TuristaBooking turistaBooking){
        return mDatabase.child(turistaBooking.getIdTurista()).setValue(turistaBooking);
    }

    public Task<Void> updateStatus(String idTuristaBooking, String estado){
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("estado",estado);

        return mDatabase.child(idTuristaBooking).updateChildren(mapa);

    }

    public DatabaseReference obtenerEstado(String idTuristaBooking){
        return mDatabase.child(idTuristaBooking).child("estado");
    }

}
