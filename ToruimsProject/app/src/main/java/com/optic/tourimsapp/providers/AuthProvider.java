package com.optic.tourimsapp.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthProvider {

    //Para la autenticación y validación con Firebase
    FirebaseAuth mAuth;

    //Realizar la instancia de Firebase
    public AuthProvider(){
        mAuth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> registrar (String correoElectronico, String contraseña){
        return mAuth.createUserWithEmailAndPassword(correoElectronico,contraseña);
    }

    public Task<AuthResult> loguearUsuario (String correoElectronico, String contraseña){
        return mAuth.signInWithEmailAndPassword(correoElectronico,contraseña);
    }

    public void cerrarSesion (){
        mAuth.signOut();
    }



}
