package com.optic.tourimsapp.modelos;

public class GuiaTuristico {

    String id;
    String nombreCompleto;
    String correoElectronico;
    String imagen;

    public String getId() {
        return id;
    }

    public GuiaTuristico(String id, String nombreCompleto, String correoElectronico) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.correoElectronico = correoElectronico;
    }

    public GuiaTuristico(){}

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }
}
