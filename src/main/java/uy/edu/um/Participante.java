package edu.uy.um;

public class Participante {
    private String nombre;
    private String rol; // actor, director, etc.
    private String peliculaId;

    public Participante(String nombre, String rol, String peliculaId) {
        this.nombre = nombre;
        this.rol = rol;
        this.peliculaId = peliculaId;
    }
}
