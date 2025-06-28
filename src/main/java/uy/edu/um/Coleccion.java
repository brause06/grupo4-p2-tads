package edu.uy.um;

import edu.uy.um.linkedlist.MyList;

public class Coleccion {
    private String id;
    private String titulo;
    private MyList<String> peliculasIds;
    private double ingresosTotales;

    public Coleccion(String id, String titulo, MyList<String> peliculasIds, double ingresosTotales) {
        this.id = id;
        this.titulo = titulo;
        this.peliculasIds = peliculasIds;
        this.ingresosTotales = ingresosTotales;
    }

}
