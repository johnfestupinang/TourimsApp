package com.optic.tourimsapp.activities.Turistas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.Utilidades.DecodePoints;
import com.optic.tourimsapp.modelos.FCMBody;
import com.optic.tourimsapp.modelos.FCMResponse;
import com.optic.tourimsapp.modelos.TuristaBooking;
import com.optic.tourimsapp.providers.AuthProvider;
import com.optic.tourimsapp.providers.GeofireProvider;
import com.optic.tourimsapp.providers.GoogleApiProvider;
import com.optic.tourimsapp.providers.NotificationProvider;
import com.optic.tourimsapp.providers.TokenProvider;
import com.optic.tourimsapp.providers.TuristaBookingProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestGuiaTuristicoActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView txtViewLookingFor;
    private Button btnCancelarSolicitud;

    private GeofireProvider mGiofireProvider;
    private NotificationProvider mNotificationProvider;
    private TokenProvider mTokenProvider;
    private TuristaBookingProvider mTuristaBookingProvider;
    private AuthProvider mAuthProvider;
    private GoogleApiProvider mGoogleApiProvider;

    private String Origen;//mExtraOrigin
    private String Destino;//mExtraDestination

    private double mExtraOrigenLat;
    private double mExtraOrigenLong;

    private double mExtraDestinoLat;
    private double mExtraDestinoLong;


    private LatLng mOriginLatLong;
    private LatLng mDestinationLatLong;
    private double radio = 0.1f;

    private boolean mGuiaEncontrado = false; //mdriverfound
    private String mIdGuiaTuristico = "";//mIdDriverFound
    private LatLng mGuiaFoundLatLng; //mDriverFoundLatLng

    private ValueEventListener mListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_guia_turistico);

        mAnimation = findViewById(R.id.animacion);
        txtViewLookingFor = findViewById(R.id.txtViewLookingFor);
        btnCancelarSolicitud = findViewById(R.id.btnCancelarSolicitud);

        mAnimation.playAnimation();

        mGiofireProvider = new GeofireProvider("guias_turisticos_activos");
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();
        mTuristaBookingProvider = new TuristaBookingProvider();
        mAuthProvider = new AuthProvider();
        mGoogleApiProvider = new GoogleApiProvider(RequestGuiaTuristicoActivity.this);

        Origen = getIntent().getStringExtra("origen");
        Destino = getIntent().getStringExtra("destino");
        mExtraDestinoLat = getIntent().getDoubleExtra("destino_lat",0);
        mExtraDestinoLong = getIntent().getDoubleExtra("destino_lng",0);

        mExtraOrigenLat = getIntent().getDoubleExtra("origen_lat",0);
        mExtraOrigenLong = getIntent().getDoubleExtra("origen_lng",0);
        Log.e("POSICION_ORIGEN","POSICION, "+mExtraOrigenLong+" "+mExtraOrigenLat);
        mOriginLatLong = new LatLng(mExtraOrigenLat,mExtraOrigenLong);
        mDestinationLatLong = new LatLng(mExtraDestinoLat,mExtraDestinoLong);

        this.obtenerGuiasTuristicosCercanos();

    }

    //getClosestDriver
    private void obtenerGuiasTuristicosCercanos(){

        Log.e("ERROR","ENTRO A obtenerGuiasActivos");
        mGiofireProvider.obtenerGuiasActivos(mOriginLatLong, radio).addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.e("NO ERROR","Entro al onKeyEntered");
                //EMPIEZA LA BUSQUEDA DEL GUIA TURISTICO
                if(!mGuiaEncontrado){
                    mGuiaEncontrado = true;
                    mIdGuiaTuristico = key;
                    mGuiaFoundLatLng = new LatLng(location.latitude, location.longitude);
                    txtViewLookingFor.setText("GUIA ENCONTRADO\nESPERANDO RESPUESTA");
                    createTuristaBooking();
                    //sendNotification();
                    Log.d("GUIA","ID"+mIdGuiaTuristico);

                }else{
                    Log.e("ERROR","Estoy en onKeyEntered");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Log.e("ERROR","Entro al onGeoQueryReady");
                //INGRESA ACA CUANDO YA ENCONTRO ALGUN GUIA EN EL RADIO DE 0.1Km
                if(!mGuiaEncontrado){
                    radio = radio + 0.1f;

                    // No encontro ningun guia
                    if(radio > 5){
                        txtViewLookingFor.setText("NO SE ENCONTRO GUIA TURISTICO");
                        Toast.makeText(RequestGuiaTuristicoActivity.this, "NO SE ENCONTRO GUIA TURISTICO", Toast.LENGTH_LONG).show();
                        return;
                    }else{
                        obtenerGuiasTuristicosCercanos();
                    }
                }else{
                    Log.e("ERROR","Mensaje de onGeoQueryReady");
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        Log.e("ERROR","TERMINO del metodo donde busca guias cercanos");

    }

    private void createTuristaBooking(){

        // Indica la distancia entre el origen y el destino -> mOriginLatLong,mDestinationLatLong
        mGoogleApiProvider.getDirections(mOriginLatLong,mGuiaFoundLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray =  jsonObject.getJSONArray("routes");
                    JSONObject route  = jsonArray.getJSONObject(0);
                    JSONObject polyLines = route.getJSONObject("overview_polyline");
                    String points = polyLines.getString("points");

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject instance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanciaTexto = instance.getString("text");//Obtener el texto de la distancia 3 km
                    String duracionTexto = duration.getString("text");//obtener el texto de la duracion 5min

                    sendNotification(duracionTexto,distanciaTexto);



                }catch(Exception e){
                    Log.e("Error","Error encontrado: "+e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });



    }

    private void sendNotification(String tiempo, String distancia) {//distancia en KM
        mTokenProvider.getToken(mIdGuiaTuristico).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {//El snapshot devuelve todo el elemnte en este caso el del nodo token
                if(snapshot.exists()){
                    String tokenUsuario = snapshot.child("token").getValue().toString();//Retorna el token asociado al idUsuario
                    Map<String, String> mapaMensaje = new HashMap<>();
                    mapaMensaje.put("title","SOLICITUD DE GUIA TURISTICO A "+tiempo+" DE SU POSICION");
                    mapaMensaje.put("body","Hay un turista cercano solicitando sus servicios a una distancia de: "+distancia
                            +"\n.Recoger en: "+Origen+"\n.Destino: "+Destino+".");
                    mapaMensaje.put("idTurista",mAuthProvider.getId());

                    FCMBody body = new FCMBody(tokenUsuario,"high","4500s",mapaMensaje);
                    mNotificationProvider.sendNotification(body).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() == 1){
                                    Toast.makeText(RequestGuiaTuristicoActivity.this, "Su notificacion se ha enviado correctamente", Toast.LENGTH_LONG).show();

                                    TuristaBooking turistaBooking = new TuristaBooking(
                                            mAuthProvider.getId(),
                                            mIdGuiaTuristico,
                                            Destino,
                                            Origen,
                                            tiempo,
                                            distancia,
                                            "creado",
                                            mExtraOrigenLat,
                                            mExtraOrigenLong,
                                            mExtraDestinoLat,
                                            mExtraDestinoLong

                                    );

                                    mTuristaBookingProvider.create(turistaBooking).addOnSuccessListener((OnSuccessListener) (aVoid) -> {
                                        checkStatusTuristaBooking();
                                        Toast.makeText(RequestGuiaTuristicoActivity.this, "El Guia acepto la solicitud",Toast.LENGTH_LONG);
                                    });

                                    /*mTuristaBookingProvider.create(turistaBooking).addOnSuccessListener(new OnSuccessListener<Void>() {

                                     @Override
                                       public void onSuccess(Void unused) {
                                            Toast.makeText(RequestGuiaTuristicoActivity.this, "Peticion creada correctamente",Toast.LENGTH_LONG);
                                        }

                                    });*/

                                }else{
                                    Toast.makeText(RequestGuiaTuristicoActivity.this, "Ocurrio un error al enviar la notificacion.", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(RequestGuiaTuristicoActivity.this, "Ocurrio un error al enviar la notificacion.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("Error","Error Notificaciones: "+t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(RequestGuiaTuristicoActivity.this, "No se pudo enviar la notificacion, porque el guia turistico no tiene un token de sesion.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkStatusTuristaBooking(){
       mListener = mTuristaBookingProvider.obtenerEstado(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {//funcionara para obtener la informacion en tiempo real
             if(snapshot.exists()){
                 String estado = snapshot.getValue().toString();
                 if(estado.equals("aceptado")){
                     Intent intent = new Intent(RequestGuiaTuristicoActivity.this, MapTuristaBookingActivity.class);
                     startActivity(intent);
                     finish();
                 }else if(estado.equals("cancelado")){
                     Toast.makeText(RequestGuiaTuristicoActivity.this,"El guia turistico no acepto hacer el recorrido",Toast.LENGTH_LONG).show();
                     Intent intent = new Intent(RequestGuiaTuristicoActivity.this, MapTuristaActivity.class);
                     startActivity(intent);
                     finish();
                 }
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {//Metodo que pertenece al ciclo de vida de Android
        super.onDestroy();
        if(mListener != null){//Con el fin de dejar de escuchar los cambios de la BD
            mTuristaBookingProvider.obtenerEstado(mAuthProvider.getId()).removeEventListener(mListener);
        }

    }
}