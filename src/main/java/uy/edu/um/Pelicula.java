package edu.uy.um;
import edu.uy.um.linkedlist.MyList;
import java.util.Date;

public class Pelicula {
    private String id;
    private String titulo;
    private String idiomaOriginal;
    private MyList<String> generos;
    private String idColeccion;
    private String tituloColeccion;
    private double ingresos;
    private Date fechaEstreno;

    public Pelicula(String id, String titulo, String idiomaOriginal, MyList<String> generos,
                    String idColeccion, String tituloColeccion, double ingresos, Date fechaEstreno) {
        this.id = id;
        this.titulo = titulo;
        this.idiomaOriginal = idiomaOriginal;
        this.generos = generos;
        this.idColeccion = idColeccion;
        this.tituloColeccion = tituloColeccion;
        this.ingresos = ingresos;
        this.fechaEstreno = fechaEstreno;
    }

    public String toString() {
        return id + ", " + titulo;
    }
}
