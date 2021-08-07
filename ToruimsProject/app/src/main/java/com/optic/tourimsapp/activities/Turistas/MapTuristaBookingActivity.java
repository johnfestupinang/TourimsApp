package com.optic.tourimsapp.activities.Turistas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.Utilidades.DecodePoints;
import com.optic.tourimsapp.activities.GuiaTuristicos.MapGuiaBookingActivity;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.GeofireProvider;
import com.optic.tourimsapp.providers.GoogleApiProvider;
import com.optic.tourimsapp.providers.GuiaTuristicoProvider;
import com.optic.tourimsapp.providers.TokenProvider;
import com.optic.tourimsapp.providers.TuristaBookingProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapTuristaBookingActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private GeofireProvider mGeofireProvider;
    private TokenProvider mTokenProvider;
    private AuthProvider mAuthProvider;
    private TuristaBookingProvider mTuristaBookingProvider;
    private GuiaTuristicoProvider mGuiaProvider;


    private Marker mMarkerGuia;//Marcador para saber la posicion
    private boolean mIsFirtsTime = true;

    private String lugarOrigen;//mOrigin
    private LatLng latLngOrigen;//mOriginLatLng

    private String lugarDestino;//mDestination
    private LatLng latLngDestino;//mdestinationLatLng
    private LatLng mGuiaLatLng;

    private TextView txtTuristaBooking;
    private TextView txtEmailTuristaBooking;
    private TextView txtOrigenTuristaBooking;
    private TextView txtDestinoTuristaBooking;
    private TextView txtViewEstadoSolicitud;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private ValueEventListener mListener;
    private String mIdGuia;
    private ValueEventListener mListenerEstado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_turista_booking);


        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync((OnMapReadyCallback) this);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("guias_turisticos_trabajando");
        mTokenProvider = new TokenProvider();
        mTuristaBookingProvider = new TuristaBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapTuristaBookingActivity.this);
        mGuiaProvider = new GuiaTuristicoProvider();


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        txtTuristaBooking = findViewById(R.id.txtViewGuiaBooking);
        txtEmailTuristaBooking = findViewById(R.id.txtViewEmailGuiaBooking);
        txtOrigenTuristaBooking = findViewById(R.id.txtViewOrigenGuiaBooking);
        txtDestinoTuristaBooking = findViewById(R.id.txtViewDestinoGuiaBooking);
        txtViewEstadoSolicitud = findViewById(R.id.txtViewEstadoSolicitud);

        obtenerEstado();
        obtnerTuristaBooking();

    }

    private void obtenerEstado() {
        mListenerEstado = mTuristaBookingProvider.obtenerEstado(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String estado = snapshot.getValue().toString();
                    if(estado.equals("aceptado")){
                        txtViewEstadoSolicitud.setText("Estado: Recorrido aceptado");
                    }
                    if(estado.equals("iniciado")){
                        txtViewEstadoSolicitud.setText("Estado: Recorrido iniciado");
                        iniciarBooking();
                    }else if(estado.equals("finalizado")){
                        txtViewEstadoSolicitud.setText("Estado: Recorrido finalizado");
                        finalizarBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void finalizarBooking() {
        Intent intent = new Intent(MapTuristaBookingActivity.this, CalificacionGuiaActivity.class);
        startActivity(intent);
        finish();
    }

    private void iniciarBooking() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLngDestino).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_mappin)));
        drawRoute(latLngDestino);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener != null){
            //Para no escuchar la posicion del guia cuando se cierre la pantalla del mapa
            mGeofireProvider.obtenerLocalizacionGuia(mIdGuia).removeEventListener(mListener);
        }
        if(mListenerEstado != null){
            mTuristaBookingProvider.obtenerEstado(mAuthProvider.getId()).removeEventListener(mListenerEstado);
        }
    }

    private void obtnerTuristaBooking() {
        mTuristaBookingProvider.obtenerTuristaBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String destino = snapshot.child("destino").getValue().toString();
                    String origen = snapshot.child("origen").getValue().toString();
                    String idGuia = snapshot.child("idGuiaTuristico").getValue().toString();
                    mIdGuia = idGuia;
                    Log.e("idGuia", "TAG: "+idGuia );

                    double destinoLat = Double.parseDouble(snapshot.child("destinoLat").getValue().toString());
                    double destinoLng = Double.parseDouble(snapshot.child("destinoLng").getValue().toString());

                    double origenLat = Double.parseDouble(snapshot.child("origenLat").getValue().toString());
                    double origenLng = Double.parseDouble(snapshot.child("origenLng").getValue().toString());

                    latLngOrigen = new LatLng(origenLat,origenLng);
                    latLngDestino = new LatLng(destinoLat,destinoLng);

                    txtOrigenTuristaBooking.setText("Recoger en: "+origen);
                    txtDestinoTuristaBooking.setText("Destino: "+destino);
                    mMap.addMarker(new MarkerOptions().position(latLngOrigen).title("Encontrar aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_mappin)));
                    obtenerDatosGuiaTuristico(idGuia);
                    obtenerLocalizacionGuiaT(idGuia);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void obtenerDatosGuiaTuristico(String idGuia){
        mGuiaProvider.obtenerGuia(idGuia).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String nombre = snapshot.child("nombreCompleto").getValue().toString();
                    String email = snapshot.child("correoElectronico").getValue().toString();

                    txtTuristaBooking.setText(nombre);
                    txtEmailTuristaBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void obtenerLocalizacionGuiaT(String idGuia) {
        mListener = mGeofireProvider.obtenerLocalizacionGuia(idGuia).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    double latitud = Double.parseDouble(snapshot.child("0").getValue().toString());
                    double longuitud = Double.parseDouble(snapshot.child("1").getValue().toString());
                    mGuiaLatLng = new LatLng(latitud,longuitud);
                    if(mMarkerGuia != null){
                        mMarkerGuia.remove();
                    }
                    mMarkerGuia = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitud,longuitud))
                            .title("Guia turistico")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_marcadorguiaturistico)));

                    if(mIsFirtsTime){//Para trazar la ruta de distancia entre el guia y el turista una sola vez
                        mIsFirtsTime = false;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mGuiaLatLng)//Donde se tiene que enfocar
                                        .zoom(14f).build()
                        ));
                        drawRoute(latLngOrigen);
                    }
                }else{
                    Log.e("Localizacion", "NO EXISTE NODO CON ESE ID " );
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //DIBUJA LA RUTA
    private void drawRoute(LatLng latLng){
        mGoogleApiProvider.getDirections(mGuiaLatLng,latLng).enqueue(new Callback<String>() {
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


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }


}