package uy.edu.um.clases;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Usuario implements Comparable<Usuario> {

    private String idUsuario;
    private MyHash<Genero, Integer> calificacionesPorGenero; // para cada género, cuántas calificaciones dio este usuario

    // Campo para comparación dinámica por género específico
    private Genero generoComparacion = null;

    public Usuario(String idUsuario) {
        this.idUsuario = idUsuario;
        this.calificacionesPorGenero = new MyHashImpl<>();
    }

    public void setGeneroComparacion(Genero genero) {
        this.generoComparacion = genero;
    }

    @Override
    public int compareTo(Usuario otro) {
        // Si no hay género configurado, comparar por ID
        if (this.generoComparacion == null) {
            return this.idUsuario.compareTo(otro.idUsuario);
        }

        // Obtener calificaciones del género configurado
        Integer misCalificaciones = this.calificacionesPorGenero.get(this.generoComparacion);
        Integer otrasCalificaciones = otro.calificacionesPorGenero.get(this.generoComparacion);

        // Tratar null como 0
        int misCalif = (misCalificaciones != null) ? misCalificaciones : 0;
        int otrasCalif = (otrasCalificaciones != null) ? otrasCalificaciones : 0;

        // Comparar por calificaciones (mayor es "menor" para max heap)
        int resultado = Integer.compare(otrasCalif, misCalif);
        
        // Si son iguales, usar ID como desempate
        if (resultado == 0) {
            resultado = this.idUsuario.compareTo(otro.idUsuario);
        }
        
        return resultado;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return Objects.equals(idUsuario, usuario.idUsuario);  // Solo el ID que no cambia
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);  // Solo el ID que no cambia
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario='" + idUsuario + '\'' +
                ", calificacionesPorGenero=" + calificacionesPorGenero.size() + " géneros" +
                '}';
    }
}
