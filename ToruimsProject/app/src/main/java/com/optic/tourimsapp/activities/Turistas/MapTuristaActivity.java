package com.optic.tourimsapp.activities.Turistas;

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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.activities.GuiaTuristicos.MapGuiaTuristicoActivity;
import com.optic.tourimsapp.activities.MainActivity;
import com.optic.tourimsapp.includes.MyToolbar;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.GeofireProvider;
import com.optic.tourimsapp.providers.TokenProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapTuristaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGSREQUEST_CODE = 2;

    private Marker mMarker;//Marcador para saber la posicion
    private LatLng mCurrenLatLng;

    private List<Marker> mGuias = new ArrayList<>();

    private boolean mIsFirtsTime = true;

    private AutocompleteSupportFragment mAutoComplete;
    private AutocompleteSupportFragment mAutoCompleteDestination;
    private PlacesClient mPlaces;

    private String lugarOrigen;//mOrigin
    private LatLng latLngOrigen;//mOriginLatLng

    private String lugarDestino;//mDestination
    private LatLng latLngDestino;//mdestinationLatLng

    private GoogleMap.OnCameraIdleListener mCameralistener;

    private Button btnSolicitarGuia ;

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    /*if(mMarker != null){
                        mMarker.remove();
                    }*/

                    mCurrenLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    /*mMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(),location.getLongitude())
                            )
                            .title("Tu posicion")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_marcadorturista))
                    );*/
                    //Obtener la localizacion del usuario en tiempo real
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f)
                                    .build()

                    ));

                    if (mIsFirtsTime) {
                        mIsFirtsTime = false;
                        obtenerGuiasActivos();
                        limitarBusqueda();
                    }
                }
            }
        }
    };

    AuthProvider mAuthProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_turista);

        MyToolbar.show(MapTuristaActivity.this, "Mapa Turista", false);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("guias_turisticos_activos");
        mTokenProvider = new TokenProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(MapTuristaActivity.this);

        btnSolicitarGuia = findViewById(R.id.BtnSolicitarGuia);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mPlaces = Places.createClient(MapTuristaActivity.this);

        //Origen
        instanciarAutoCompliteOrigen();

        //Destino
        instanciarAutoComplitDestino();

        //Camera listener
        onCamaraMove();


        btnSolicitarGuia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solicitarGuia();
            }
        });

        generarToken();

    }

    private void limitarBusqueda(){
        LatLng northSide = SphericalUtil.computeOffset(mCurrenLatLng, 5000,0);
        LatLng southSide = SphericalUtil.computeOffset(mCurrenLatLng, 5000,180);
        mAutoComplete.setCountry("COL");
        mAutoComplete.setLocationBias(RectangularBounds.newInstance(southSide,northSide));
        mAutoCompleteDestination.setCountry("COL");
        mAutoCompleteDestination.setLocationBias(RectangularBounds.newInstance(southSide,northSide));
    }

    private void onCamaraMove(){
        mCameralistener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geoCoder = new Geocoder(MapTuristaActivity.this);
                    latLngOrigen = mMap.getCameraPosition().target;
                    List<Address> listaDirecciones = geoCoder.getFromLocation(latLngOrigen.latitude, latLngOrigen.longitude, 1);
                    String ciudad = listaDirecciones.get(0).getLocality();
                    String pais = listaDirecciones.get(0).getCountryName();
                    String direccion = listaDirecciones.get(0).getAddressLine(0);
                    lugarOrigen = direccion+" "+ciudad;
                    mAutoComplete.setText(direccion+" "+ciudad);
                }catch (Exception e){
                    Log.e("Error: ","Mensaje Error: "+e.getMessage());
                }
            }
        };
    }

    private void solicitarGuia(){
        if(latLngOrigen != null && latLngDestino != null){
            Intent intent = new Intent(MapTuristaActivity.this, DetailRequestActivity.class);
            intent.putExtra("origin_lat",latLngOrigen.latitude);
            intent.putExtra("origin_lng",latLngOrigen.longitude);
            intent.putExtra("destination_lat",latLngDestino.latitude);
            intent.putExtra("destination_lng",latLngDestino.longitude);
            intent.putExtra("origen", lugarOrigen);
            intent.putExtra("destino", lugarDestino);
            startActivity(intent);

        }else{
            Toast.makeText(this, "Seleccione el lugar de origen y destino", Toast.LENGTH_LONG).show();
        }
    }

    private void instanciarAutoCompliteOrigen(){
        //Origen
        mAutoComplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigin);
        mAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutoComplete.setHint("Origen");
        mAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                lugarOrigen = place.getName();
                latLngOrigen = place.getLatLng();
                Log.d("PLACE: ", "Name: " + lugarOrigen);
                Log.d("PLACE: ", "LATLNG: " + latLngOrigen.latitude + " - " + latLngOrigen.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void instanciarAutoComplitDestino(){
        //Destino
        mAutoCompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestination);
        mAutoCompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutoCompleteDestination.setHint("Destino");
        mAutoCompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                lugarDestino = place.getName();
                latLngDestino = place.getLatLng();
                Log.d("PLACE: ", "Name: " + lugarDestino);
                Log.d("PLACE: ", "LATLNG: " + latLngDestino.latitude + " - " + latLngDestino.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void obtenerGuiasActivos() {
        mGeofireProvider.obtenerGuiasActivos(mCurrenLatLng,10).addGeoQueryEventListener(new GeoQueryEventListener() {

            //añadir los marcadores de los guias turisticos que se van conectando
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //AÑADIR LOS MARCADORES DE LOS GUIAS QUE SE CONECTAN
                for (Marker marker : mGuias) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }
                }

                LatLng posicionGuiaQueSeConecto = new LatLng(location.latitude, location.longitude);

                Marker marker = mMap.addMarker(new MarkerOptions().position(posicionGuiaQueSeConecto)
                        .title("Guia turistico disponible")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_marcadorguiaturistico)));
                marker.setTag(key);
                mGuias.add(marker);
            }

            //eliminar los marcadores que se desconectan de la aplicacion
            @Override
            public void onKeyExited(String key) {
                //ELIMINAR DE LA LISTA DE MARCADORES LOS GUIAS QUE SE DESCONECTAN
                for (Marker marker : mGuias) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            mGuias.remove(marker);
                            return;
                        }
                    }
                }
            }

            //Posicion en tiempo real del guia turistico a medida de que se mueve
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                //ACTUALIZAR POSICIONDE CADA GUIA TURISTICO
                for (Marker marker : mGuias) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(mCameralistener);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MapTuristaActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if(gpsActived()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
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
            mMap.setMyLocationEnabled(true);
        }else if (requestCode == SETTINGSREQUEST_CODE && !gpsActived()){
            showAlertDialogNOGPS();
        }
    }

    private void showAlertDialogNOGPS(){//Metodo para mostrar cuadro de dialogo en caso de que no se tenga el GPS activado
        AlertDialog.Builder builder = new AlertDialog.Builder(MapTuristaActivity.this);

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

    private void startLocation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(MapTuristaActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(gpsActived()){
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }else{
                    showAlertDialogNOGPS();
                }

            }else{
                checkLocationPermissions();
            }
        }else{
            if(gpsActived()){
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else{
                showAlertDialogNOGPS();
            }
        }
    }

    private void checkLocationPermissions(){
        if(ContextCompat.checkSelfPermission(MapTuristaActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MapTuristaActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(MapTuristaActivity.this)
                        .setTitle("Tourims App: Permisos de Localizacion")
                        .setMessage("Esta aplicacion necesita los permisos de localizacion para poder utilizarse")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapTuristaActivity.this
                                        ,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();

            }else{
                ActivityCompat.requestPermissions(MapTuristaActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.turista_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.accion_cerrarSesion){
            cerrarSesion();
        }
        if(item.getItemId() == R.id.accion_actualizarPerfil){
            Intent intent = new Intent(MapTuristaActivity.this,ActualizarPerfilActivity.class);
            startActivity(intent);
        }
        if(item.getItemId() == R.id.accion_historial){
            Intent intent = new Intent(MapTuristaActivity.this,HistoryBookingTuristaActivity.class);
            startActivity(intent);
        }



        return super.onOptionsItemSelected(item);
    }

    void cerrarSesion(){
        mAuthProvider.cerrarSesion();
        Intent intent = new Intent(MapTuristaActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //TOKEN PARA GENERAR LA NOTIFICACION
    void generarToken(){
        mTokenProvider.crear(mAuthProvider.getId());
    }
}