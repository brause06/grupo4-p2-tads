package uy.edu.um.clases;

import lombok.Data;
import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;
import uy.edu.um.tad.linkedlist.MyList;

import java.time.LocalDate;

@Data
public class Pelicula implements Comparable<Pelicula> {
    private String idPelicula;
    private String titulo;
    private String idiomaOriginal;
    private int mesEstreno;  // Solo almacenar el mes (1-12) en lugar de fecha completa
    private long ingresos;
    private Coleccion coleccion;
    private MyList<Genero> generos;

    private MyList<Actor> actoresParticipantes;  // todos los actores que aparecen en esta película

    private Director directorPrincipal;  // el director principal (puede ser null)

    private int totalCalificaciones;  // cuántas calificaciones recibió en total
    private double sumaCalificaciones;  // suma de todas las puntuaciones (para calcular promedio)
    private MyHash<Integer, Integer> calificacionesPorMes; // para cada mes (1-12) cuántas calificaciones recibió

    public Pelicula(String idPelicula, String titulo, String idiomaOriginal, long ingresos, LocalDate releaseDate) {
        this.idPelicula = idPelicula;
        this.titulo = titulo;
        this.idiomaOriginal = idiomaOriginal;
        this.ingresos = ingresos;
        this.mesEstreno = (releaseDate != null) ? releaseDate.getMonthValue() : 0; // Solo extraer y guardar el mes
        this.generos = new MyLinkedListImpl<>();
        this.actoresParticipantes = new MyLinkedListImpl<>();
        this.calificacionesPorMes = new MyHashImpl<>();
        this.totalCalificaciones = 0;
        this.sumaCalificaciones = 0.0;
    }

    // Esta variable me permite cambiar cómo se comparan las películas
    private static boolean compararPorMedia = false;
    
    // Para cambiar temporalmente el criterio de comparación si necesito usar heaps de diferentes tipos
    public final static void setCompararPorMedia(boolean valor) {
        compararPorMedia = valor;  // false = por total de calificaciones, true = por calificación media
    }
    
    @Override
    public int compareTo(Pelicula otra) {
        if (compararPorMedia) {
            // Comparo por calificación media (útil para la consulta 2)
            double mediaEsta = this.totalCalificaciones > 0 ? this.sumaCalificaciones / this.totalCalificaciones : 0.0;
            double mediaOtra = otra.totalCalificaciones > 0 ? otra.sumaCalificaciones / otra.totalCalificaciones : 0.0;
            return Double.compare(mediaEsta, mediaOtra);
        } else {
            // Comparo por total de calificaciones (útil para la consulta 1)
            return Integer.compare(this.totalCalificaciones, otra.totalCalificaciones);
        }
    }

}
