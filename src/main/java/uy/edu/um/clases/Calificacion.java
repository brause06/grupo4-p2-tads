package uy.edu.um.clases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Calificacion implements Comparable<Calificacion> {
    
    private String idUsuario;
    private String idPelicula;
    private double puntuacion; // tiene que estar entre 0 y 5
    private LocalDateTime timestamp; // guardo cuándo se hizo la calificación (para consultas por mes)
    
    // Constructor alternativo si no tengo el timestamp (pone la fecha actual)
    public Calificacion(String idUsuario, String idPelicula, double puntuacion) {
        this.idUsuario = idUsuario;
        this.idPelicula = idPelicula;
        this.puntuacion = puntuacion;
        this.timestamp = LocalDateTime.now();
    }
    
    // Me da el mes (1-12) de cuándo se hizo esta calificación
    public int getMes() {
        return timestamp.getMonthValue();
    }
    
    // Me da el año de cuándo se hizo esta calificación
    public int getAno() {
        return timestamp.getYear();
    }
    
    // Para poder comparar calificaciones por puntuación
    @Override
    public int compareTo(Calificacion other) {
        return Double.compare(this.puntuacion, other.puntuacion);
    }
    
    // Verifico que la puntuación esté en el rango correcto
    public boolean esPuntuacionValida() {
        return puntuacion >= 0.0 && puntuacion <= 5.0;
    }
} 