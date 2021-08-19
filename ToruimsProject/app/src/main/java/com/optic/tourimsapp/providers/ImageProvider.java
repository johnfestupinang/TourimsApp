package com.optic.tourimsapp.providers;

import android.content.Context;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.optic.tourimsapp.Utilidades.CompressorBitmapImage;

import java.io.File;

public class ImageProvider {

    private StorageReference mStorage;

    public ImageProvider(String referencia){
        mStorage = FirebaseStorage.getInstance().getReference().child(referencia);

    }

    public UploadTask saveImage(String idUsuario, Context contexto, File imagen){
        byte[] imageByte = CompressorBitmapImage.getImage(contexto, imagen.getPath(),500,500);
        StorageReference storage = mStorage.child(idUsuario+".jpg");
        mStorage = storage;
        UploadTask uploadTask = storage.putBytes(imageByte);
        return uploadTask;

    }

    public StorageReference obtenerStorage(){
        return mStorage;
    }

}
