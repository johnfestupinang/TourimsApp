package com.optic.tourimsapp.activities.GuiaTuristicos;

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
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.Turistas.RegisterActivity;
import com.optic.tourimsapp.includes.MyToolbar;
import com.optic.tourimsapp.modelos.GuiaTuristico;
import com.optic.tourimsapp.modelos.Turista;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.GuiaTuristicoProvider;
import com.optic.tourimsapp.providers.TuristaProvider;

import dmax.dialog.SpotsDialog;

public class RegisterGuiaTuristicoActivity extends AppCompatActivity {

    //Llamar a los providers
    AuthProvider mAuthProvider;
    GuiaTuristicoProvider mGuiaturisticoProvider;

    Button btnRegistrar;
    TextInputEditText txtNombre;
    TextInputEditText txtCorreoElectronico;
    TextInputEditText txtContrasena;

    //Para el loader
    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_guia_turistico);

        //Mostrar ToolBar
        MyToolbar.show(RegisterGuiaTuristicoActivity.this,"Registrar Guia Turistico",true);

        //Instanciar los providers
        mAuthProvider = new AuthProvider();
        mGuiaturisticoProvider = new GuiaTuristicoProvider();

        mDialog = new SpotsDialog.Builder().setContext(RegisterGuiaTuristicoActivity.this).setMessage("Cargando...").build();

        txtNombre = findViewById(R.id.txtNombre);
        txtCorreoElectronico = findViewById(R.id.txtCorreoElectronico);
        txtContrasena = findViewById(R.id.txtContrasena);
        btnRegistrar = findViewById(R.id.btnRegistrar);


        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRegistrarUsuario();
            }
        });
    }

    private void clickRegistrarUsuario() {
        final String nombreCompleto = txtNombre.getText().toString();
        final String correoElectronico = txtCorreoElectronico.getText().toString();
        final String contraseña = txtContrasena.getText().toString();

        Log.e("","********************************");
        Log.e("nombreCompleto_",nombreCompleto);
        Log.e("correoElectronico",correoElectronico);
        Log.e("contraseña",contraseña);
        Log.e("","********************************");

        if(!nombreCompleto.isEmpty() && !correoElectronico.isEmpty() && !contraseña.isEmpty()){
            if(contraseña.length() >= 6){
                mDialog.show();
                registrar(nombreCompleto,correoElectronico,contraseña);

            }else{
                Toast.makeText(this,"¡La contraseña debe ser minimo de 6 caracteres",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"¡Todos los campos son obligatorios!",Toast.LENGTH_SHORT).show();
        }
    }

    void registrar(final String nombreCompleto, String correoElectronico, String contraseña){
        //logica del registro de usuarios
        mAuthProvider.registrar(correoElectronico,contraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if(task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    GuiaTuristico nuevoGuia = new GuiaTuristico(id, nombreCompleto, correoElectronico);
                    crear(nuevoGuia);

                    //saveUser(nombreCompleto, correoElectronico,id);
                    Log.e("isSuccessful","Entro al exitoso");
                }else{
                    Toast.makeText(RegisterGuiaTuristicoActivity.this,"¡No se pudo registrar el usuario!",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    //Solo para turistas
    public void crear(GuiaTuristico nuevoGuiaTuristico){
        mGuiaturisticoProvider.crearGuiaTuristico(nuevoGuiaTuristico).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull  Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(RegisterGuiaTuristicoActivity.this, MapGuiaTuristicoActivity.class);
                    //Evitar que cuando se registre y sea exitoso vuelva a la pantalla anterior
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Toast.makeText(RegisterGuiaTuristicoActivity.this, "!Registro GUIA TURISTICO exitosamente, Bienvenido¡", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(RegisterGuiaTuristicoActivity.this, "!No se pudo crear el Guia Turistico¡", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}