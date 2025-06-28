package uy.edu.um.clases;

import lombok.Data;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;
import uy.edu.um.tad.linkedlist.MyList;

@Data
public class Director implements Comparable<Director> {

    private String idDirector;
    private String nombreDirector;

    private MyList<Pelicula> peliculasDirigidas;  // todas las películas que dirigió

    private MyList<Double> calificacionesDeSusPeliculas; // acá voy juntando todas las calificaciones de sus películas (para después calcular la mediana)

    public Director(String idDirector, String nombreDirector) {
        this.idDirector = idDirector;
        this.nombreDirector = nombreDirector;
        this.peliculasDirigidas = new MyLinkedListImpl<>();
        this.calificacionesDeSusPeliculas = new MyLinkedListImpl<>();
    }
    
    // Método para calcular la mediana de las calificaciones de este director
    public double getMediana() {
        if (calificacionesDeSusPeliculas.size() == 0) {
            return 0.0;
        }
        
        // Convertir MyList a array para ordenar
        double[] calificaciones = new double[calificacionesDeSusPeliculas.size()];
        for (int i = 0; i < calificacionesDeSusPeliculas.size(); i++) {
            calificaciones[i] = calificacionesDeSusPeliculas.get(i);
        }
        
        // Ordenamiento burbuja simple
        for (int i = 0; i < calificaciones.length - 1; i++) {
            for (int j = 0; j < calificaciones.length - 1 - i; j++) {
                if (calificaciones[j] > calificaciones[j + 1]) {
                    double temp = calificaciones[j];
                    calificaciones[j] = calificaciones[j + 1];
                    calificaciones[j + 1] = temp;
                }
            }
        }
        
        // Calcular mediana
        int n = calificaciones.length;
        if (n % 2 == 0) {
            // Si es par, promedio de los dos valores centrales
            return (calificaciones[n/2 - 1] + calificaciones[n/2]) / 2.0;
        } else {
            // Si es impar, valor central
            return calificaciones[n/2];
        }
    }
    
    @Override
    public int compareTo(Director otro) {
        // Comparar por mediana de calificaciones (para heap)
        if (this.calificacionesDeSusPeliculas.size() == 0 && otro.calificacionesDeSusPeliculas.size() == 0) {
            return 0;
        }
        if (this.calificacionesDeSusPeliculas.size() == 0) {
            return -1;
        }
        if (otro.calificacionesDeSusPeliculas.size() == 0) {
            return 1;
        }
        
        double miMediana = this.getMediana();
        double otraMediana = otro.getMediana();
        
        return Double.compare(miMediana, otraMediana);
    }
}
