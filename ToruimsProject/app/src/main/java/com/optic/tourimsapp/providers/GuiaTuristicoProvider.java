package com.optic.tourimsapp.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.tourimsapp.modelos.GuiaTuristico;
import com.optic.tourimsapp.modelos.Turista;

import java.util.HashMap;
import java.util.Map;

public class GuiaTuristicoProvider {

    //Para acceder a la BD de Firebase
    DatabaseReference mDatabase;

    public GuiaTuristicoProvider(){
        //Instancia de BD haciendo referencia a los nodos Usuario y GuiasTuristicos
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Usuario").child("GuiasTuristicos");
    }

    public Task<Void> crearGuiaTuristico(GuiaTuristico nuevoGuiaTuristico){
        return mDatabase.child(nuevoGuiaTuristico.getId()).setValue(nuevoGuiaTuristico);
    }

    public Task<Void> actualizarGuia(GuiaTuristico guiaActualizado){

        Map<String, Object> guiaActualizad = new HashMap<>();
        //Asignar los valores al mapa
        guiaActualizad.put("Id", guiaActualizado.getId());
        guiaActualizad.put("nombreCompleto",guiaActualizado.getNombreCompleto());
        guiaActualizad.put("Imagen",guiaActualizado.getImagen());

        //Enviar el mapa con los campos seteados a Firebase
        return mDatabase.child(guiaActualizado.getId()).updateChildren(guiaActualizad);
    }

    public DatabaseReference obtenerGuia(String idGuia) {
        return mDatabase.child(idGuia);
    }
}
