package com.optic.tourimsapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.Turistas.MapTuristaActivity;

public class MainActivity extends AppCompatActivity {

    Button btnSoyTurista;
    Button btnSoyGuiaTuristico;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSoyGuiaTuristico = findViewById(R.id.btnSoyGuiaTuristico);
        btnSoyTurista = findViewById(R.id.btnSoyTurista);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();


        btnSoyTurista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user","turista");
                editor.apply();
                irSeleccionarAutenticacion();
            }
        });

        btnSoyGuiaTuristico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("user","guia");
                editor.apply();
                irSeleccionarAutenticacion();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){//En algún caso de que exista ya una sesión ya iniciada
            String tipoUsuario = mPref.getString("user","");
            switch (tipoUsuario){
                case "turista":
                    Intent intentT = new Intent(MainActivity.this, MapTuristaActivity.class);
                    //Evitar que cuando se registre y sea exitoso vuelva a la pantalla anterior
                    intentT.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentT);
                    break;
                case "guia":
                    Intent intentG = new Intent(MainActivity.this, MapTuristaActivity.class);
                    //Evitar que cuando se registre y sea exitoso vuelva a la pantalla anterior
                    intentG.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentG);
                    break;
                default:
                    break;
            }

        }
    }

    private void irSeleccionarAutenticacion(){
        Intent intent = new Intent(MainActivity.this, SelectOptionAuthActivity.class);
        startActivity(intent);
    }
}