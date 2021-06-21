package com.optic.tourimsapp.activities.GuiaTuristicos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.MainActivity;
import com.optic.tourimsapp.activities.Turistas.MapTuristaActivity;
import com.optic.tourimsapp.includes.MyToolbar;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.GeofireProvider;

public class MapGuiaTuristicoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGSREQUEST_CODE = 2;

    private Marker mMarker;//Marcador para saber la posicion

    private Button btnConectarse;
    private boolean estoyConectado = false;

    private LatLng mCurrentLatLng;


    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mCurrentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                    if(mMarker != null){
                        mMarker.remove();
                    }


                    mMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(),location.getLongitude())
                            )
                            .title("Tu posicion")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_marcadorguiaturistico))
                    );

                    //Obtener la localizacion del usuario en tiempo real
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()

                    ));
                    actualizarLocalizacion();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_guia_turistico);

        MyToolbar.show(MapGuiaTuristicoActivity.this, "Mapa Guia Turistico", false);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(MapGuiaTuristicoActivity.this);

        btnConectarse = findViewById(R.id.btnConectarse);

        btnConectarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(estoyConectado){
                    desconectado();
                }else{
                    startLocation();
                }
            }
        });

    }

    private void actualizarLocalizacion(){
        if(mAuthProvider.existeSesion() && mCurrentLatLng != null){
            mGeofireProvider.guardarLocalizacion(mAuthProvider.getId(), mCurrentLatLng);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MapGuiaTuristicoActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(gpsActived()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        }
                        mMap.setMyLocationEnabled(true); //Habilitar el marcador por defecto
                    }else{
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGSREQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            }
            mMap.setMyLocationEnabled(true); //Habilitar el marcador por defecto
        }else{
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS(){//Metodo para mostrar cuadro de dialogo en caso de que no se tenga el GPS activado
        AlertDialog.Builder builder = new AlertDialog.Builder(MapGuiaTuristicoActivity.this);

        builder.setMessage("Por favor active la ubicacion para continuar")
        .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        SETTINGSREQUEST_CODE
                        );
            }
        })
                .create()
                .show();

    }

    //verificar si el GPS esta activado
    private boolean gpsActived(){
        boolean isActived = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isActived = true;
        }
        return isActived;
    }

    private void desconectado(){

        if(mFusedLocation != null){
            btnConectarse.setText("Conectarse");
            estoyConectado = false;
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if(mAuthProvider.existeSesion()){
                mGeofireProvider.eliminarLocalizacion(mAuthProvider.getId());
            }

        }else{
            Toast.makeText(MapGuiaTuristicoActivity.this,"No se puede desconectar", Toast.LENGTH_LONG);
        }


    }

    private void startLocation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(MapGuiaTuristicoActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(gpsActived()){
                    btnConectarse.setText("Desconectarse");
                    estoyConectado = true;
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    }
                    mMap.setMyLocationEnabled(true); //Habilitar el marcador por defecto
                }else{
                    showAlertDialogNOGPS();
                }

            }else{
                checkLocationPermissions();
            }
        }else{
            if(gpsActived()){
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
                mMap.setMyLocationEnabled(true); //Habilitar el marcador por defecto
            }else{
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions(){
        if(ContextCompat.checkSelfPermission(MapGuiaTuristicoActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MapGuiaTuristicoActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(MapGuiaTuristicoActivity.this)
                        .setTitle("Tourims App: Permisos de Localizacion")
                        .setMessage("Esta aplicacion necesita los permisos de localizacion para poder utilizarse")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapGuiaTuristicoActivity.this
                                        ,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_REQUEST_CODE);
                            }
                        })
                .create()
                .show();

            }else{
                ActivityCompat.requestPermissions(MapGuiaTuristicoActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.guiaturistico_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.accion_cerrarSesion){
            cerrarSesion();
        }
        return super.onOptionsItemSelected(item);
    }

    void cerrarSesion(){
        desconectado();
        mAuthProvider.cerrarSesion();
        Intent intent = new Intent(MapGuiaTuristicoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}