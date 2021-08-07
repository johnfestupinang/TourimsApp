package com.optic.tourimsapp.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.tourimsapp.modelos.HistorialBooking;
import com.optic.tourimsapp.modelos.TuristaBooking;

import java.util.HashMap;
import java.util.Map;

public class HistorialBookingProvider {
    DatabaseReference mDatabase;

    public HistorialBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("HistorialBooking");
    }

    public Task<Void> create(HistorialBooking historialBooking){
        return mDatabase.child(historialBooking.getIdHistorialBooking()).setValue(historialBooking);
    }

    public Task<Void> actualizarCalificacionTurista(float calificacionTurista, String idHistorialBooking){
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("calificacionTurista",calificacionTurista);
        return mDatabase.child(idHistorialBooking).updateChildren(mapa);
    }

    public Task<Void> actualizarCalificacionGuia(float calificacionGuia, String idHistorialBooking){
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("calificacionGuia",calificacionGuia);
        return mDatabase.child(idHistorialBooking).updateChildren(mapa);
    }

    public DatabaseReference obtenerHisotrialBooking(String idHistorialBooking){
        return mDatabase.child(idHistorialBooking);
    }


}
