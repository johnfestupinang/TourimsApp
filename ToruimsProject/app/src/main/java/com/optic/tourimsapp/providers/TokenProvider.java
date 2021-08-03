package com.optic.tourimsapp.providers;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.optic.tourimsapp.modelos.Token;


public class TokenProvider {

    DatabaseReference mDatabase;

    public TokenProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void crear(String idUsuarioLogeado){
        if(idUsuarioLogeado == null )return;
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                Token token = new Token(task.getResult());
                mDatabase.child(idUsuarioLogeado).setValue(token);
            }
        });
    }

    public DatabaseReference getToken(String idUsuario){
        return mDatabase.child(idUsuario);
    }
}
