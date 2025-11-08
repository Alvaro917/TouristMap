package com.example.touristmap.map;

public class Lugar {
    private final String nombre;
    private final double latitud;
    private final double longitud;
    private final int iconoResId;
    private final String categoria;

    public Lugar(String nombre, double latitud, double longitud, int iconoResId, String categoria) {
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.iconoResId = iconoResId;
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }
    public double getLatitud() {
        return latitud;
    }
    public double getLongitud() {
        return longitud;
    }
    public int getIconoResId() {
        return iconoResId;
    }
    public String getCategoria() {
        return categoria;
    }
}

