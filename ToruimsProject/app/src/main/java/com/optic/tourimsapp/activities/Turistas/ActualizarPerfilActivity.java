package com.optic.tourimsapp.activities.Turistas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.Utilidades.CompressorBitmapImage;
import com.optic.tourimsapp.includes.MyToolbar;
import com.optic.tourimsapp.modelos.Turista;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.ImageProvider;
import com.optic.tourimsapp.providers.TuristaProvider;
import com.optic.transporteapp.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ActualizarPerfilActivity extends AppCompatActivity {

    private ImageView txtViewPerfil;
    private Button btnActualizar;
    private TextView txtNombre;


    private TuristaProvider mTuristaProvider;
    private AuthProvider mAuthProvider;
    private ImageProvider mImageProvider;

    private File mImageFile;
    private String mImage;//URL que almacena la imagen en firebase

    private final int  GALLERY_REQUEST = 1;
    private ProgressDialog mProgressDialog;

    private String nombreActualizado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar_perfil);

        MyToolbar.show(this, "Actualizar Perfil", true);

        txtViewPerfil = findViewById(R.id.txtviewPerfil);
        btnActualizar = findViewById(R.id.btnActualizarPerfil);
        txtNombre = findViewById(R.id.txtNombre);

        mTuristaProvider = new TuristaProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImageProvider("turistas_imagenes");

        mProgressDialog = new ProgressDialog(this);

        obtenerInformacionTurista();//Para traer la informacion del turista

        txtViewPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abrir la galeria de imagenes del telefono
                openGallery();
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarPerfil();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            try {
                mImageFile = FileUtil.from(this, data.getData());
                txtViewPerfil.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            }catch (Exception e){
                Log.d("ERROR","Mensaje: "+e.getMessage());
            }
        }
    }

    private void obtenerInformacionTurista(){
        mTuristaProvider.obtenerCliente(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombreCompleto").getValue().toString();
                    String imagen = "";
                    if(snapshot.hasChild("Imagen")){
                        imagen = snapshot.child("Imagen").getValue().toString();
                        Picasso.with(ActualizarPerfilActivity.this).load(imagen).into(txtViewPerfil);
                    }
                    txtNombre.setText(nombre);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void actualizarPerfil() {

        nombreActualizado = txtNombre.getText().toString();
        Log.e("NOMBRE: ","Nombre: "+nombreActualizado);
        Log.e("mIMAGENFILE: ","Nombre: "+mImageFile);
        if(!nombreActualizado.equals("") && mImageFile != null){
            mProgressDialog.setMessage("Espere unos segundos...");
            mProgressDialog.setCanceledOnTouchOutside(false);//para evitar que el turista cancele la subida de la imagen
            mProgressDialog.show();

            //Usando la dependencia de FIREBASE
            saveImage();

        }else{
            Toast.makeText(this, "Debe ingresar NOMBRE Y LA IMAGEN", Toast.LENGTH_LONG).show();
        }
    }

    private void saveImage() {
        mImageProvider.saveImage(mAuthProvider.getId(),ActualizarPerfilActivity.this,mImageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){//si es exitosa, es porque se pudo subir la imagen a Firebase Storage
                    mImageProvider.obtenerStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imagen = uri.toString();
                            //Incluir el campo en el nodo Turistas
                            Turista turista = new Turista();
                            turista.setImagen(imagen);
                            turista.setNombreCompleto(nombreActualizado);
                            turista.setId(mAuthProvider.getId());
                            mTuristaProvider.actualizarTurista(turista).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(ActualizarPerfilActivity.this,"Se actualizo la informacion correctamente",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }else{
                    Toast.makeText(ActualizarPerfilActivity.this,"Ocurrio un error al intentar subir la imagen",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}