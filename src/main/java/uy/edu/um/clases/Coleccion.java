package uy.edu.um.clases;


import lombok.Data;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;
import uy.edu.um.tad.linkedlist.MyList;

@Data
public class Coleccion implements Comparable<Coleccion> {

    private String idColeccion;
    private String tituloColeccion;
    private long ingresosTotales;  // esto lo voy a calcular sumando los ingresos de todas las películas de la colección

    private MyList<Pelicula> peliculas;  // todas las películas que pertenecen a esta colección

    public Coleccion(String idColeccion, String tituloColeccion) {
        this.idColeccion = idColeccion;
        this.tituloColeccion = tituloColeccion;
        this.ingresosTotales = 0;
        this.peliculas = new MyLinkedListImpl<>();
    }
    
    @Override
    public int compareTo(Coleccion otra) {
        // Para poder usar colecciones en heaps, las comparo por ingresos totales
        return Long.compare(this.ingresosTotales, otra.ingresosTotales);
    }
}
