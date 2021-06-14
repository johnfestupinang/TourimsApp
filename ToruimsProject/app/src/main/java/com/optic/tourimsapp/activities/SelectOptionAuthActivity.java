package com.optic.tourimsapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.GuiaTuristicos.RegisterGuiaTuristicoActivity;
import com.optic.tourimsapp.activities.Turistas.RegisterActivity;


public class SelectOptionAuthActivity extends AppCompatActivity {

    Toolbar mToolbar;
    Button btnYaTengoCuenta;//btnGoToLogin
    Button btnRegistrarmeAhora;//btnGoToRegister

    //Para identificar el rol a que pertenecen
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);

        mToolbar = findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Seleccionar Opción");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Define si el toolbar tendrá un botón para devolverse

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        btnYaTengoCuenta = findViewById(R.id.btnYaTengoCuenta);
        btnYaTengoCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAlLogin();
            }
        });

        btnRegistrarmeAhora = findViewById(R.id.btnRegistrarmeAhora);
        btnRegistrarmeAhora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irAlRegistrarmeAhora();
            }
        });

    }

    private void irAlLogin() { //Iniciar Sesion
        Intent intent = new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        startActivity(intent);

    }

    @SuppressLint("ShowToast")
    private void irAlRegistrarmeAhora() {
        String tipoUsuario = mPref.getString("user","");
        switch (tipoUsuario){
            case "turista":
                Intent intentT = new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
                startActivity(intentT);
                break;
            case "guia":
                Intent intentG = new Intent(SelectOptionAuthActivity.this, RegisterGuiaTuristicoActivity.class);
                startActivity(intentG);
                break;
            default:
                Toast.makeText(SelectOptionAuthActivity.this,"!Error al seleccionar, por favor verifique¡", Toast.LENGTH_LONG);
                break;
        }

    }
}