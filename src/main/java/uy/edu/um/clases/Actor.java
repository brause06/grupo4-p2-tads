package uy.edu.um.clases;

import lombok.Getter;
import lombok.Setter;
import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;
import uy.edu.um.tad.linkedlist.MyList;
import java.util.Objects;

@Getter
@Setter
public class Actor implements Comparable<Actor> {

    private String idActor;
    private String nombreActor;

    private MyList<Pelicula> peliculasEnLasQueActua;  // todas las películas donde aparece este actor

    private MyHash<Integer, Integer> calificacionesRecibidasPorMes; // para cada mes (1-12) cuántas calificaciones recibió

    // Campo para comparación dinámica por mes específico
    private Integer mesComparacion = null;

    public Actor(String idActor, String nombreActor) {
        this.idActor = idActor;
        this.nombreActor = nombreActor;
        this.peliculasEnLasQueActua = new MyLinkedListImpl<>();
        this.calificacionesRecibidasPorMes = new MyHashImpl<>();
    }
    
    public int getCantidadPeliculasEnMes(int mes) {
        // Usar hash temporal como Set para películas únicas de este actor en este mes
        MyHash<String, Boolean> peliculasUnicasDelMes = new MyHashImpl<>();
        
        for (int i = 0; i < peliculasEnLasQueActua.size(); i++) {
            Pelicula pelicula = peliculasEnLasQueActua.get(i);
            
            // Verificar si esta película mía tiene calificaciones en este mes
            Integer calificacionesDelMes = pelicula.getCalificacionesPorMes().get(mes);
            if (calificacionesDelMes != null && calificacionesDelMes > 0) {
                peliculasUnicasDelMes.put(pelicula.getIdPelicula(), true);
            }
        }
        
        return peliculasUnicasDelMes.size(); // cantidad de películas mías calificadas en este mes
    }

    // Configurar el mes para comparación en heap
    public void setMesComparacion(int mes) {
        this.mesComparacion = mes;
    }

    @Override
    public int compareTo(Actor otro) {
        // Si no hay mes configurado, comparar por ID (para tener orden consistente)
        if (this.mesComparacion == null) {
            return this.idActor.compareTo(otro.idActor);
        }

        // Obtener calificaciones del mes configurado
        Integer misCalificaciones = this.calificacionesRecibidasPorMes.get(this.mesComparacion);
        Integer otrasCalificaciones = otro.calificacionesRecibidasPorMes.get(this.mesComparacion);

        // Tratar null como 0
        int misCalif = (misCalificaciones != null) ? misCalificaciones : 0;
        int otrasCalif = (otrasCalificaciones != null) ? otrasCalificaciones : 0;

        // Comparar por calificaciones (mayor es "menor" para max heap)
        int resultado = Integer.compare(otrasCalif, misCalif);
        
        // Si son iguales, usar ID como desempate
        if (resultado == 0) {
            resultado = this.idActor.compareTo(otro.idActor);
        }
        
        return resultado;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Actor actor = (Actor) obj;
        return Objects.equals(idActor, actor.idActor);  // Solo el ID que no cambia
    }

    @Override
    public int hashCode() {
        return Objects.hash(idActor);  // Solo el ID que no cambia
    }
    
    @Override
    public String toString() {
        return "Actor{" +
                "idActor='" + idActor + '\'' +
                ", nombreActor='" + nombreActor + '\'' +
                ", peliculas=" + peliculasEnLasQueActua.size() +
                ", calificacionesPorMes=" + calificacionesRecibidasPorMes.size() + " meses" +
                '}';
    }
}
