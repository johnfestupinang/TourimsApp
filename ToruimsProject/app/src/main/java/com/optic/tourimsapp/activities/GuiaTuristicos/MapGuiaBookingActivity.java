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
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.Utilidades.DecodePoints;
import com.optic.tourimsapp.activities.Turistas.DetailRequestActivity;
import com.optic.tourimsapp.activities.Turistas.RequestGuiaTuristicoActivity;
import com.optic.tourimsapp.modelos.FCMBody;
import com.optic.tourimsapp.modelos.FCMResponse;
import com.optic.tourimsapp.modelos.TuristaBooking;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.GeofireProvider;
import com.optic.tourimsapp.providers.GoogleApiProvider;
import com.optic.tourimsapp.providers.NotificationProvider;
import com.optic.tourimsapp.providers.TokenProvider;
import com.optic.tourimsapp.providers.TuristaBookingProvider;
import com.optic.tourimsapp.providers.TuristaProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapGuiaBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private AuthProvider mAuthProvider;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private TuristaProvider mTuristaProvider;
    private TuristaBookingProvider mTuristaBookingProvider;
    private NotificationProvider mNotificationProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGSREQUEST_CODE = 2;

    private Marker mMarker;//Marcador para saber la posicion

    private LatLng mCurrentLatLng;

    private TextView txtTuristaBooking;
    private TextView txtEmailTuristaBooking;
    private TextView txtOrigenTuristaBooking;
    private TextView txtDestinoTuristaBooking;

    private String mExtraTuristaId;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private boolean mIsFirtsTime = true;
    private boolean mEstaCercaAlTurista = false;

    private Button btnIniciarBooking;
    private Button btnFinalizarBooking;


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
                    if (mIsFirtsTime) {
                        mIsFirtsTime = false;
                        obtnerTuristaBooking();//Obtiene el turistaBooking del nodod TuristaBooking (SOLICITUD DEL VIAJE)
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_guia_booking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("guias_turisticos_trabajando");
        mTokenProvider = new TokenProvider();
        mTuristaProvider = new TuristaProvider();
        mTuristaBookingProvider = new TuristaBookingProvider();
        mNotificationProvider = new NotificationProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        txtTuristaBooking = findViewById(R.id.txtViewTuristaBooking);
        txtEmailTuristaBooking = findViewById(R.id.txtViewEmailTuristaBooking);
        txtOrigenTuristaBooking = findViewById(R.id.txtViewOrigenTuristaBooking);
        txtDestinoTuristaBooking = findViewById(R.id.txtViewDestinoTuristaBooking);

        btnIniciarBooking = findViewById(R.id.btnIniciarBooking);
        //btnIniciarBooking.setEnabled(false);

        btnFinalizarBooking = findViewById(R.id.btnIFinalizarBooking);


        mExtraTuristaId = getIntent().getStringExtra("idTurista");
        mGoogleApiProvider = new GoogleApiProvider(MapGuiaBookingActivity.this);

        obtenerCliente();//Obtiene el tyrista del nodo Usuarios -> Turista
        //obtnerTuristaBooking();//Obtiene el turistaBooking del nodod TuristaBooking (SOLICITUD DEL VIAJE)

        btnIniciarBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEstaCercaAlTurista){
                    iniciarBooking();
                }else{
                    Toast.makeText(MapGuiaBookingActivity.this, "Usted debe estar mas cerca al Turista.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnFinalizarBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalizarBooking();
            }

        });

    }

    private void finalizarBooking() {
        mTuristaBookingProvider.updateStatus(mExtraTuristaId,"finalizado");
        mTuristaBookingProvider.updateidHistoryBooking(mExtraTuristaId);
        sendNotification("recorrido finalizado");
        if(mFusedLocation != null){
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        }
        mGeofireProvider.eliminarLocalizacion(mAuthProvider.getId());
        Intent intent = new Intent(MapGuiaBookingActivity.this, CalificacionTuristaActivity.class);
        intent.putExtra("idTurista",mExtraTuristaId);
        startActivity(intent);
        finish();
    }

    private void iniciarBooking(){
        mTuristaBookingProvider.updateStatus(mExtraTuristaId,"iniciado");
        btnIniciarBooking.setVisibility(View.GONE);
        btnFinalizarBooking.setVisibility(View.VISIBLE);
        mMap.clear();//Eliminar el marcador y la ruta trazada
        Log.e("ERROR","DESTINO: "+mDestinationLatLng);
        drawRoute(mDestinationLatLng);
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_mappin)));
        sendNotification("recorrido iniciado.");
    }

    private double ObtenerDistanciaEntre(LatLng posicionRecogidaTurista, LatLng posicionActualGuia){
        double distancia = 0;

        Location turistaLocation = new Location("");
        Location guiaLocation = new Location("");

        turistaLocation.setLatitude(posicionRecogidaTurista.latitude);
        turistaLocation.setLongitude(posicionRecogidaTurista.longitude);

        guiaLocation.setLatitude(posicionActualGuia.latitude);
        guiaLocation.setLongitude(posicionActualGuia.longitude);

        distancia = turistaLocation.distanceTo(guiaLocation);//Obtener la distancia entre los dos

        return distancia;
    }

    private void obtnerTuristaBooking() {
        mTuristaBookingProvider.obtenerTuristaBooking(mExtraTuristaId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destino = snapshot.child("destino").getValue().toString();
                    String origen = snapshot.child("origen").getValue().toString();

                    double destinoLat = Double.parseDouble(snapshot.child("destinoLat").getValue().toString());
                    double destinoLng = Double.parseDouble(snapshot.child("destinoLng").getValue().toString());

                    double origenLat = Double.parseDouble(snapshot.child("origenLat").getValue().toString());
                    double origenLng = Double.parseDouble(snapshot.child("origenLng").getValue().toString());

                    mOriginLatLng = new LatLng(origenLat,origenLng);
                    mDestinationLatLng = new LatLng(destinoLat,destinoLng);

                    txtOrigenTuristaBooking.setText("Recoger en: "+origen);
                    txtDestinoTuristaBooking.setText("Destino: "+destino);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Encontrar aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_mappin)));
                    drawRoute(mOriginLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //DIBUJA LA RUTA
    private void drawRoute(LatLng latLng){//Hacia donde quiero trazar la ruta
        mGoogleApiProvider.getDirections(mCurrentLatLng,latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray =  jsonObject.getJSONArray("routes");
                    JSONObject route  = jsonArray.getJSONObject(0);
                    JSONObject polyLines = route.getJSONObject("overview_polyline");
                    String points = polyLines.getString("points");

                    mPolyLineList = DecodePoints.decodePoly(points);
                    mPolyLineOptions = new PolylineOptions();
                    //mPolyLineOptions.color(Color.DKGRAY);
                    mPolyLineOptions.color(Color.rgb(51,50,88));
                    mPolyLineOptions.width(13f);
                    mPolyLineOptions.startCap(new SquareCap());
                    mPolyLineOptions.jointType(JointType.ROUND);
                    mPolyLineOptions.addAll(mPolyLineList);
                    mMap.addPolyline(mPolyLineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject instance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanciaTexto = instance.getString("text");//Obtener el texto de la distancia 3 km
                    String duracionTexto = duration.getString("text");//obtener el texto de la duracion 5min



                }catch(Exception e){
                    Log.e("Error","Error encontrado: "+e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void obtenerCliente() {

        mTuristaProvider.obtenerCliente(mExtraTuristaId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String email = snapshot.child("correoElectronico").getValue().toString();
                    String nombre = snapshot.child("nombreCompleto").getValue().toString();
                    txtTuristaBooking.setText(nombre);
                    txtEmailTuristaBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void actualizarLocalizacion(){
        if(mAuthProvider.existeSesion() && mCurrentLatLng != null){
            mGeofireProvider.guardarLocalizacion(mAuthProvider.getId(), mCurrentLatLng);
            if(!mEstaCercaAlTurista){//Si el guia no esta cerca al turista
                if((mOriginLatLng!=null)&&(mCurrentLatLng!=null)){
                    double distancia = ObtenerDistanciaEntre(mOriginLatLng,mCurrentLatLng);//Posicion del turista, posicion actual del Guia
                    if(distancia <= 200){
                        //btnIniciarBooking.setEnabled(true);
                        mEstaCercaAlTurista = true;
                        Toast.makeText(this,"Usted está cerca a la posición del Turista",Toast.LENGTH_LONG).show();
                    }

                }




            }

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

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if(mAuthProvider.existeSesion()){
                mGeofireProvider.eliminarLocalizacion(mAuthProvider.getId());
            }

        }else{
            Toast.makeText(this,"No se puede desconectar", Toast.LENGTH_LONG);
        }


    }

    private void startLocation(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if(gpsActived()){
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
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Tourims App: Permisos de Localizacion")
                        .setMessage("Esta aplicacion necesita los permisos de localizacion para poder utilizarse")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapGuiaBookingActivity.this
                                        ,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();

            }else{
                ActivityCompat.requestPermissions(MapGuiaBookingActivity.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }
        }
    }

    private void sendNotification(String estado){
        mTokenProvider.getToken(mExtraTuristaId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {//El snapshot devuelve todo el elemnte en este caso el del nodo token
                if(snapshot.exists()){
                    String tokenUsuario = snapshot.child("token").getValue().toString();//Retorna el token asociado al idUsuario
                    Map<String, String> mapaMensaje = new HashMap<>();
                    mapaMensaje.put("title","ESTADO DE TU RECORRIDO");
                    mapaMensaje.put("body","El estado del reccorido turistico es: "+estado+".");

                    FCMBody body = new FCMBody(tokenUsuario,"high","4500s",mapaMensaje);
                    mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() != 1){
                                    Toast.makeText(MapGuiaBookingActivity.this, "Ocurrio un error al enviar la notificacion.", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(MapGuiaBookingActivity.this, "Ocurrio un error al enviar la notificacion.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("Error","Error Notificaciones: "+t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(MapGuiaBookingActivity.this, "No se pudo enviar la notificacion, porque el guia turistico no tiene un token de sesion.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}