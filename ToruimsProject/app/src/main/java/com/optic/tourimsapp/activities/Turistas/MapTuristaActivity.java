package com.optic.tourimsapp.activities.Turistas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.MainActivity;
import com.optic.tourimsapp.providers.AuthProvider;

public class MapTuristaActivity extends AppCompatActivity {

    Button btnCerrarSesion;
    AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_turista);

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        mAuthProvider = new AuthProvider();

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mAuthProvider.cerrarSesion();
                Intent intent = new Intent(MapTuristaActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}