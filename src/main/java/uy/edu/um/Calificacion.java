package edu.uy.um;

import java.util.Date;

public class Calificacion {
    private int usuarioId;
    private String peliculaId;
    private double puntaje;
    private Date fecha;

    public Calificacion(int usuarioId, String peliculaId, double puntaje, Date fecha) {
        this.usuarioId = usuarioId;
        this.peliculaId = peliculaId;
        this.puntaje = puntaje;
        this.fecha = fecha;
    }
}
