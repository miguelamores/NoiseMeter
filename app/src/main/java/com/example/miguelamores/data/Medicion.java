package com.example.miguelamores.data;

import java.util.Date;

/**
 * Created by miguelamores on 7/2/15.
 */
public class Medicion {
    private int medicion_id;
    private double valor_db;
    private double latitud;
    private double longitud;
    private Date hora;
    private int usuario_id;

    public int getMedicion_id() {
        return medicion_id;
    }

    public void setMedicion_id(int medicion_id) {
        this.medicion_id = medicion_id;
    }

    public double getValor_db() {
        return valor_db;
    }

    public void setValor_db(double valor_db) {
        this.valor_db = valor_db;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public Date getHora() {
        return hora;
    }

    public void setHora(Date hora) {
        this.hora = hora;
    }

    public int getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(int usuario_id) {
        this.usuario_id = usuario_id;
    }
}
