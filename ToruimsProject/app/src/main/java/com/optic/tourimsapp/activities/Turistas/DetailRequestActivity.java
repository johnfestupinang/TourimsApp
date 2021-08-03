package com.optic.tourimsapp.activities.Turistas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.optic.tourimsapp.R;
import com.optic.tourimsapp.Utilidades.DecodePoints;
import com.optic.tourimsapp.includes.MyToolbar;
import com.optic.tourimsapp.providers.GoogleApiProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    //Variables a las que se les asignara el valor del origen y destino
    private double mExtraOriginLat;
    private double mExtraOriginLong;
    private double mExtraDestinationLat;
    private double mExtraDestinationLong;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolyLineList;
    private PolylineOptions mPolyLineOptions;

    private TextView txtViewOrigen;//mTextViewOrigin
    private TextView txtViewDestino;//mTextViewOrigin
    private TextView txtViewTiempo;//mTextViewTime
    private TextView txtViewDistancia;//mTextViewDistance

    private String origen;
    private String destino;

    private Button btnsolicitarAhora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);
        MyToolbar.show(this, "Tú ruta turística",true);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLong = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLong = getIntent().getDoubleExtra("destination_lng", 0);
        origen = getIntent().getStringExtra("origen");
        destino = getIntent().getStringExtra("destino");

        Log.e("ORIGEN LAT",""+mExtraOriginLat);
        Log.e("ORIGEN LNG",""+mExtraOriginLong);
        Log.e("DESTINO LAT",""+mExtraDestinationLat);
        Log.e("DESTINO LNG",""+mExtraDestinationLong);

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLong);
        mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLong);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);

        txtViewOrigen = findViewById(R.id.txtViewOrigen);
        txtViewDestino = findViewById(R.id.txtViewDestino);
        txtViewTiempo = findViewById(R.id.txtViewTiempo);
        txtViewDistancia = findViewById(R.id.txtViewDistancia);

        txtViewOrigen.setText(origen);
        txtViewDestino.setText(destino);
        Log.e("ORIGEN",""+origen);
        Log.e("DESTINO",""+destino);

        btnsolicitarAhora = findViewById(R.id.btnsolicitarAhora);

        btnsolicitarAhora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                irASolicitarTurista(); //goToRequestDriver
            }
        });




    }

    private void irASolicitarTurista(){
        Intent intent = new Intent(DetailRequestActivity.this, RequestGuiaTuristicoActivity.class);
        intent.putExtra("origen_lat",mOriginLatLng.latitude);
        intent.putExtra("origen_lng",mOriginLatLng.longitude);
        intent.putExtra("origen",origen);
        intent.putExtra("destino",destino);
        intent.putExtra("destino_lat",mExtraDestinationLat);
        intent.putExtra("destino_lng",mExtraDestinationLong);
        startActivity(intent);
        finish();//Cerrar esta actividad cuando pase a la otra actividad
    }

    private void drawRoute(){
        mGoogleApiProvider.getDirections(mOriginLatLng,mDestinationLatLng).enqueue(new Callback<String>() {
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
                    //txtViewDistancia.setText(distanciaTexto);
                    txtViewDistancia.setText(distanciaTexto);
                    txtViewTiempo.setText(duracionTexto);

                    Log.e("distanciaTexto",""+distanciaTexto);
                    Log.e("txtViewTiempo",""+distanciaTexto);


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

        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_mappin)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icono_mappin_gris)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mOriginLatLng)
                .zoom(14f).build()
        ));

        drawRoute();
    }
}