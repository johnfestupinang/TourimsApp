package com.optic.tourimsapp.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.tourimsapp.modelos.Turista;

import java.util.HashMap;
import java.util.Map;

public class TuristaProvider {

    //Para acceder a la BD de Firebase
    DatabaseReference mDatabase;

    public TuristaProvider(){
        //Instancia de BD haciendo referencia a los nodos Usuario y Turistas
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Usuario").child("Turistas");
    }

    public Task<Void> crearTurista(Turista nuevoTurista){

        Map<String, Object> mapaNuevoTurista = new HashMap<>();
        //Asignar los valores al mapa
        mapaNuevoTurista.put("Id", nuevoTurista.getId());
        mapaNuevoTurista.put("nombreCompleto",nuevoTurista.getNombreCompleto());
        mapaNuevoTurista.put("correoElectronico",nuevoTurista.getCorreoElectronico());

        //Enviar el mapa con los campos seteados a Firebase
        return mDatabase.child(nuevoTurista.getId()).setValue(mapaNuevoTurista);
    }

    public Task<Void> actualizarTurista(Turista nuevoTurista){

        Map<String, Object> turistaActualizado = new HashMap<>();
        //Asignar los valores al mapa
        turistaActualizado.put("Id", nuevoTurista.getId());
        turistaActualizado.put("nombreCompleto",nuevoTurista.getNombreCompleto());
        turistaActualizado.put("Imagen",nuevoTurista.getImagen());

        //Enviar el mapa con los campos seteados a Firebase
        return mDatabase.child(nuevoTurista.getId()).updateChildren(turistaActualizado);
    }

    public DatabaseReference obtenerCliente(String idTurista) {
        return mDatabase.child(idTurista);
    }
}
