package com.optic.tourimsapp.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {

    private DatabaseReference mDatabase;
    private GeoFire mGeofire;

    public GeofireProvider(String reference){
        mDatabase = FirebaseDatabase.getInstance().getReference().child(reference);
        //mDatabase = FirebaseDatabase.getInstance().getReference().child("guias_turisticos_activos");
        mGeofire = new GeoFire(mDatabase);
    }

    public void guardarLocalizacion(String idGuiaTuristico, LatLng latLng){
        mGeofire.setLocation(idGuiaTuristico, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    public void eliminarLocalizacion (String idGuiaTuristico){
        mGeofire.removeLocation(idGuiaTuristico);
    }

    public GeoQuery obtenerGuiasActivos(LatLng latLng,double radius){
        GeoQuery geoQuery = mGeofire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),radius);//radio de 5Km
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    //Usar la instancia para hacer referencia solo a ese objeto
    public DatabaseReference estaGuiaTrabajando(String idGuia){
        return FirebaseDatabase.getInstance().getReference().child("guias_turisticos_trabajando").child(idGuia);
    }

    public DatabaseReference obtenerLocalizacionGuia(String idGuia){
        return mDatabase.child(idGuia).child("l");
    }
}