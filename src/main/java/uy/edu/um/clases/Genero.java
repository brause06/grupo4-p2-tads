package uy.edu.um.clases;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Genero implements Comparable<Genero> {

    private String idGenero;
    private String nombreGenero;
    private int totalCalificacionesRecibidas; // cuántas calificaciones recibieron las películas de este género en total (para la consulta 6)
    
    @Override
    public int compareTo(Genero otro) {
        // Para poder usar géneros en heaps, los comparo por cuántas calificaciones tienen
        return Integer.compare(this.totalCalificacionesRecibidas, otro.totalCalificacionesRecibidas);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Genero genero = (Genero) obj;
        return Objects.equals(idGenero, genero.idGenero);  // Solo el ID que no cambia
    }

    @Override
    public int hashCode() {
        return Objects.hash(idGenero);  // Solo el ID que no cambia
    }
    
    @Override
    public String toString() {
        return "Genero{" +
                "idGenero='" + idGenero + '\'' +
                ", nombreGenero='" + nombreGenero + '\'' +
                ", totalCalificacionesRecibidas=" + totalCalificacionesRecibidas +
                '}';
    }
}
