package com.optic.tourimsapp.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.tourimsapp.modelos.GuiaTuristico;

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
}
