package com.optic.tourimsapp.includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.optic.tourimsapp.R;

public class MyToolbar {

    public static void show(AppCompatActivity actividadDondeSeMuestra, String titulo, boolean mostrarBotonAtras){

        Toolbar mToolbar = actividadDondeSeMuestra.findViewById(R.id.toolBar);
        actividadDondeSeMuestra.setSupportActionBar(mToolbar);
        actividadDondeSeMuestra.getSupportActionBar().setTitle(titulo);
        actividadDondeSeMuestra.getSupportActionBar().setDisplayHomeAsUpEnabled(mostrarBotonAtras);//Define si el toolbar tendrá un botón para devolverse

    }
}
