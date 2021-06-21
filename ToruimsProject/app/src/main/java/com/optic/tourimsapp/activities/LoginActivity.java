package com.optic.tourimsapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.GuiaTuristicos.MapGuiaTuristicoActivity;
import com.optic.tourimsapp.activities.GuiaTuristicos.RegisterGuiaTuristicoActivity;
import com.optic.tourimsapp.activities.Turistas.MapTuristaActivity;
import com.optic.tourimsapp.includes.MyToolbar;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText txtCorreoElectronico;
    TextInputEditText txtContrasena;
    Button btnIniciarSesion;

    //Para la autenticación y validación con Firebase
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    //Para identificar el rol a que pertenecen
    SharedPreferences mPref;

    //Para el loader
    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtCorreoElectronico = findViewById(R.id.txtCorreoElectronico);
        txtContrasena = findViewById(R.id.txtContrasena);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDialog = new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Iniciando Sesión...").build();

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        //Mostrar Toolbar
        MyToolbar.show(LoginActivity.this,"Iniciar Sesión",true);

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    private void login(){
        String correoElectronico = txtCorreoElectronico.getText().toString();
        String contrasena = txtContrasena.getText().toString();

        Log.e("Correo: ", correoElectronico);
        Log.e("Contraseña: ", contrasena);

        if(!correoElectronico.isEmpty() && !contrasena.isEmpty()){
            if(contrasena.length() >= 6){
                mDialog.show();
                //Funcion de logearnos en Firebase
                mAuth.signInWithEmailAndPassword(correoElectronico, contrasena).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull  Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String tipoUsuario = mPref.getString("user","");
                            if(tipoUsuario.equals("turista")){
                                Intent intent = new Intent(LoginActivity.this, MapTuristaActivity.class);
                                //Evitar que cuando se registre y sea exitoso vuelva a la pantalla anterior
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else{//Guia turistico
                                Intent intent = new Intent(LoginActivity.this, MapGuiaTuristicoActivity.class);
                                //Evitar que cuando se registre y sea exitoso vuelva a la pantalla anterior
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            Toast.makeText(LoginActivity.this, "¡Bienvenido al sistema!",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(LoginActivity.this, "¡Los datos son incorrectos, por favor verifique.!",Toast.LENGTH_SHORT).show();
                        }
                        mDialog.dismiss();
                    }
                });
            }else{
                Toast.makeText(LoginActivity.this, "¡La contraseña debe ser de minimo 6 caracteres.!",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(LoginActivity.this, "¡El correo electronico y la contraseña son obligatorios.!",Toast.LENGTH_SHORT).show();
        }
    }



}