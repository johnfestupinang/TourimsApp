package com.optic.tourimsapp.modelos;

public class HistorialBooking {

    String idHistorialBooking;
    String idTurista;
    String idGuiaTuristico;
    String destino;
    String origen;
    String tiempo;
    String distancia;
    String estado;
    double origenLat;
    double origenLng;
    double destinoLat;
    double destinoLng;
    double calificacionTurista;
    double calificacionGuia;
    long timestamp;

    public HistorialBooking(){

    }

    public HistorialBooking(String idHistorialBooking, String idTurista, String idGuiaTuristico, String destino, String origen, String tiempo, String distancia, String estado, double origenLat, double origenLng, double destinoLat, double destinoLng) {
        this.idHistorialBooking = idHistorialBooking;
        this.idTurista = idTurista;
        this.idGuiaTuristico = idGuiaTuristico;
        this.destino = destino;
        this.origen = origen;
        this.tiempo = tiempo;
        this.distancia = distancia;
        this.estado = estado;
        this.origenLat = origenLat;
        this.origenLng = origenLng;
        this.destinoLat = destinoLat;
        this.destinoLng = destinoLng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIdTurista() {
        return idTurista;
    }

    public void setIdTurista(String idTurista) {
        this.idTurista = idTurista;
    }

    public String getIdGuiaTuristico() {
        return idGuiaTuristico;
    }

    public void setIdGuiaTuristico(String idGuiaTuristico) {
        this.idGuiaTuristico = idGuiaTuristico;
    }

    public String getIdHistorialBooking() {
        return idHistorialBooking;
    }

    public void setIdHistorialBooking(String idHistorialBooking) {
        this.idHistorialBooking = idHistorialBooking;
    }

    public double getCalificacionTurista() {
        return calificacionTurista;
    }

    public void setCalificacionTurista(double calificacionTurista) {
        this.calificacionTurista = calificacionTurista;
    }

    public double getCalificacionGuia() {
        return calificacionGuia;
    }

    public void setCalificacionGuia(double getCalificacionGuia) {
        this.calificacionGuia = getCalificacionGuia;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }

    public String getDistancia() {
        return distancia;
    }

    public void setDistancia(String distancia) {
        this.distancia = distancia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getOrigenLat() {
        return origenLat;
    }

    public void setOrigenLat(double origenLat) {
        this.origenLat = origenLat;
    }

    public double getOrigenLng() {
        return origenLng;
    }

    public void setOrigenLng(double origenLng) {
        this.origenLng = origenLng;
    }

    public double getDestinoLat() {
        return destinoLat;
    }

    public void setDestinoLat(double destinoLat) {
        this.destinoLat = destinoLat;
    }

    public double getDestinoLng() {
        return destinoLng;
    }

    public void setDestinoLng(double destinoLng) {
        this.destinoLng = destinoLng;
    }

}
