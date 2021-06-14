package com.optic.tourimsapp.activities.Turistas;

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
import com.optic.tourimsapp.activities.GuiaTuristicos.MapGuiaTuristicoActivity;
import com.optic.tourimsapp.activities.GuiaTuristicos.RegisterGuiaTuristicoActivity;
import com.optic.tourimsapp.includes.MyToolbar;
import com.optic.tourimsapp.modelos.Turista;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.TuristaProvider;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    //Llamar a los providers
    AuthProvider mAuthProvider;
    TuristaProvider mTuristaProvider;

    Button btnRegistrar;
    TextInputEditText txtNombre;
    TextInputEditText txtCorreoElectronico;
    TextInputEditText txtContrasena;

    //Para el loader
    AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Mostrar ToolBar
        MyToolbar.show(RegisterActivity.this,"Registrar Turista",true);

        //Instanciar los providers
        mAuthProvider = new AuthProvider();
        mTuristaProvider = new TuristaProvider();

        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Cargando...").build();

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
                    Turista nuevoTurista = new Turista(id, nombreCompleto, contraseña);
                    crear(nuevoTurista);

                    //saveUser(nombreCompleto, correoElectronico,id);
                    Log.e("isSuccessful","Entro al exitoso");
                }else{
                    Toast.makeText(RegisterActivity.this,"¡No se pudo registrar el usuario!",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    //Solo para turistas
    public void crear(Turista nuevoTurista){
        mTuristaProvider.crearTurista(nuevoTurista).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull  Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(RegisterActivity.this, MapTuristaActivity.class);
                    //Evitar que cuando se registre y sea exitoso vuelva a la pantalla anterior
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Toast.makeText(RegisterActivity.this, "!Registro TURISTA exitosamente, Bienvenido¡", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(RegisterActivity.this, "!No se pudo crear el Turista¡", Toast.LENGTH_LONG).show();
                }

            }
        });
    }







   /* private void saveUser(String nombreCompleto, String correoElectronico, String id) {

        String usuarioSeleccionado = mPref.getString("user","");

        Log.e("usuarioSeleccionado",usuarioSeleccionado);
        Log.e("nombreCompleto",nombreCompleto);
        Log.e("correoElectronico",correoElectronico);

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(nombreCompleto);
        nuevoUsuario.setCorreoElectronico(correoElectronico);*/

       /* mDatabase.child("Usuario").child("GuiasTuristicos").child(id).setValue(nuevoUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.e("Not complete",task.toString());
                if(task.isSuccessful()){
                    Log.e("isSuccessful","debio haberlo creado guia");
                    Toast.makeText(RegisterActivity.this,"Guia turistico registrado con exito",Toast.LENGTH_SHORT).show();
                }else{
                    Log.e("isSuccessfulNOT","NO debio haberlo creado guia");
                    Toast.makeText(RegisterActivity.this,"Fallo al registrar al guia turistico ",Toast.LENGTH_SHORT).show();
                }
            }

        });*/

       /*if(usuarioSeleccionado.equals("guia")){
            Log.e("saveUser", "Entro al guia");
            mDatabase.child("Usuario").child("GuiasTuristicos").child(id).setValue(nuevoUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.e("isSuccessful","debio haberlo creado guia");
                        Toast.makeText(RegisterActivity.this,"Guia turistico registrado con exito",Toast.LENGTH_SHORT).show();
                    }else{
                        Log.e("isSuccessfulNOT","NO debio haberlo creado guia");
                        Toast.makeText(RegisterActivity.this,"Fallo al registrar al guia turistico ",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else if (usuarioSeleccionado.equals("turista")){
            Log.e("saveUser: ", "Entro al turista");
            mDatabase.child("Usuario").child("Turistas").child(id).setValue(nuevoUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Log.e("isSuccessful","debio haberlo creado turista");
                        Toast.makeText(RegisterActivity.this,"Turista registrado con exito",Toast.LENGTH_SHORT).show();
                    }else{
                        Log.e("NOisSuccessful","NO debio haberlo creado turista");
                        Toast.makeText(RegisterActivity.this,"Fallo al registrar al turista ",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            Toast.makeText(RegisterActivity.this,"estoy aca...",Toast.LENGTH_SHORT).show();
        }
    }*/
}