package uy.edu.um.clases;

import lombok.Getter;
import lombok.Setter;
import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;
import uy.edu.um.tad.linkedlist.MyList;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;

@Getter
@Setter
public class UMovieApp {
    
    // Estructuras principales
    private MyHash<String, Pelicula> peliculasPorId;
    private MyHash<String, Usuario> usuariosPorId;
    private MyHash<String, Actor> actoresPorId;
    private MyHash<String, Director> directoresPorId;
    private MyHash<String, Coleccion> coleccionesPorId;
    private MyHash<String, Genero> generosPorId;
    
    // Lista de todas las calificaciones del sistema
    private MyList<Calificacion> calificaciones;
    
    //indices
    private MyHash<Integer, MyHash<String, Actor>> actoresPorMes;     // índice: actores únicos por mes
    private MyHash<Genero, MyHash<String, Usuario>> usuariosPorGenero; // índice: usuarios únicos por género
    
    private MyHash<String, MyList<Pelicula>> peliculasPorIdioma; // índice: películas agrupadas por idioma (para consulta 1)
    private MyList<Pelicula> peliculasBienCalificadas; // índice: películas con >100 calificaciones (para consulta 2)
    private MyList<Director> directoresCalificados; // índice: directores con >1 película y >100 evaluaciones (para consulta 4)
    private MyList<Coleccion> coleccionesConIngresos; // índice: colecciones con ingresos >0 (para consulta 3)

    public UMovieApp() {
        // Inicializar estructuras principales
        peliculasPorId = new MyHashImpl<>();
        usuariosPorId = new MyHashImpl<>();
        actoresPorId = new MyHashImpl<>();
        directoresPorId = new MyHashImpl<>();
        generosPorId = new MyHashImpl<>();
        coleccionesPorId = new MyHashImpl<>();
        calificaciones = new MyLinkedListImpl<>();
        
        // Inicializar índices
        actoresPorMes = new MyHashImpl<>();
        usuariosPorGenero = new MyHashImpl<>();
        peliculasPorIdioma = new MyHashImpl<>();
        peliculasBienCalificadas = new MyLinkedListImpl<>();
        directoresCalificados = new MyLinkedListImpl<>();
        coleccionesConIngresos = new MyLinkedListImpl<>();
    }

    public void agregarPelicula(Pelicula p) {
        peliculasPorId.put(p.getIdPelicula(), p);   // solo guardo en hash para encontrarla rápido por ID
        
        // Para consulta 1: Mantener índice por idioma para búsquedas eficientes
        String idioma = p.getIdiomaOriginal();
        if (idioma != null && !idioma.isEmpty()) {
            MyList<Pelicula> peliculasDelIdioma = peliculasPorIdioma.get(idioma);
            if (peliculasDelIdioma == null) {
                peliculasDelIdioma = new MyLinkedListImpl<>();
                peliculasPorIdioma.put(idioma, peliculasDelIdioma);
            }
            peliculasDelIdioma.add(p);
        }
        
        //para consulta 3: Actualizo ingresos de colección automáticamente
        if (p.getColeccion() != null) {
            // Si la película tiene colección, sumo sus ingresos a la colección
            Coleccion coleccion = p.getColeccion();
            long ingresosAnteriores = coleccion.getIngresosTotales();
            coleccion.setIngresosTotales(coleccion.getIngresosTotales() + p.getIngresos());
            
            // Para consulta 3: Si antes no tenía ingresos y ahora sí, agregarla al índice
            if (ingresosAnteriores == 0 && coleccion.getIngresosTotales() > 0) {
                coleccionesConIngresos.add(coleccion);
            }
        } else {
            // Si no tiene colección, creo una "virtual" que representa solo esta película
            Coleccion coleccionVirtual = new Coleccion(p.getIdPelicula(), p.getTitulo());
            coleccionVirtual.setIngresosTotales(p.getIngresos());
            coleccionVirtual.getPeliculas().add(p);
            // Las colecciones virtuales se agregan al hash
            coleccionesPorId.put(coleccionVirtual.getIdColeccion(), coleccionVirtual);
            
            // Para consulta 3: Si tiene ingresos, agregarla al índice optimizado
            if (coleccionVirtual.getIngresosTotales() > 0) {
                coleccionesConIngresos.add(coleccionVirtual);
            }
        }
    }

    public void agregarUsuario(Usuario u) {
        usuariosPorId.put(u.getIdUsuario(), u);
    }

    public void agregarActor(Actor a) {
        actoresPorId.put(a.getIdActor(), a);
    }

    public void agregarDirector(Director d) {
        directoresPorId.put(d.getIdDirector(), d);
    }

    public void agregarGenero(Genero g) {
        generosPorId.put(g.getIdGenero(), g);
    }

    public void agregarColeccion(Coleccion c) {
        coleccionesPorId.put(c.getIdColeccion(), c);
    }
    
    // Este es el metodo clave que se llama cada vez que se agrega una calificación del CSV
    public void agregarCalificacion(Calificacion cal) {
        // Primero verifico que la calificación tenga sentido (entre 0 y 5)
        if (!cal.esPuntuacionValida()) {
            System.err.println("Calificación inválida: " + cal);
            return;
        }
        
        calificaciones.add(cal);
        
        // Aquí actualizo los contadores de la película que fue calificada
        Pelicula pelicula = getPeliculaPorId(cal.getIdPelicula());
        if (pelicula != null) {
            int calificacionesAnteriores = pelicula.getTotalCalificaciones();
            pelicula.setTotalCalificaciones(pelicula.getTotalCalificaciones() + 1);  // una calificación más
            pelicula.setSumaCalificaciones(pelicula.getSumaCalificaciones() + cal.getPuntuacion());  // sumo la puntuación
            
            // Para consulta 2: Si la película pasa el umbral de 100 calificaciones, agregarla al índice
            if (calificacionesAnteriores <= 100 && pelicula.getTotalCalificaciones() > 100) {
                peliculasBienCalificadas.add(pelicula);
            }
            
            // También guardo cuántas calificaciones recibió en cada mes (para consultas)
            int mes = cal.getMes();
            Integer cantidadActual = pelicula.getCalificacionesPorMes().get(mes);
            if (cantidadActual == null) cantidadActual = 0;
            pelicula.getCalificacionesPorMes().put(mes, cantidadActual + 1);
        }
        
        // Ahora actualizo las estadísticas de todos los actores de esta película
        if (pelicula != null && pelicula.getActoresParticipantes() != null) {
            int mes = cal.getMes();
            
            for (int i = 0; i < pelicula.getActoresParticipantes().size(); i++) {
                Actor actor = pelicula.getActoresParticipantes().get(i);
                
                // Sumo una calificación más a las que recibió este actor en este mes
                Integer cantidadActorMes = actor.getCalificacionesRecibidasPorMes().get(mes);
                boolean esPrimeraCalificacionDelMes = (cantidadActorMes == null);
                if (cantidadActorMes == null) cantidadActorMes = 0;
                actor.getCalificacionesRecibidasPorMes().put(mes, cantidadActorMes + 1);
                
                // Solo la primera vez que este actor recibe una calificación en este mes lo agrego a mi índice
                if (esPrimeraCalificacionDelMes) {
                    MyHash<String, Actor> actoresDelMes = actoresPorMes.get(mes);
                    if (actoresDelMes == null) {
                        actoresDelMes = new MyHashImpl<>();
                        actoresPorMes.put(mes, actoresDelMes);
                    }
                    actoresDelMes.put(actor.getIdActor(), actor); 
                }
            }
        }
        
        // Por último, actualizo las estadísticas del usuario que hizo la calificación
        Usuario usuario = getUsuarioPorId(cal.getIdUsuario());
        if (usuario != null && pelicula != null && pelicula.getGeneros() != null) {
            for (int i = 0; i < pelicula.getGeneros().size(); i++) {
                Genero genero = pelicula.getGeneros().get(i);
                Integer cantidadActual = usuario.getCalificacionesPorGenero().get(genero);
                boolean esPrimeraCalificacionDelGenero = (cantidadActual == null);
                if (cantidadActual == null) cantidadActual = 0;
                usuario.getCalificacionesPorGenero().put(genero, cantidadActual + 1);  // una calificación más para este género
                            
                // También sumo una calificación al contador total del género
                genero.setTotalCalificacionesRecibidas(genero.getTotalCalificacionesRecibidas() + 1);
                
                // Solo la primera vez que este usuario califica este género lo agrego a mi índice
                if (esPrimeraCalificacionDelGenero) {
                    MyHash<String, Usuario> usuariosDelGenero = usuariosPorGenero.get(genero);
                    if (usuariosDelGenero == null) {
                        usuariosDelGenero = new MyHashImpl<>();
                        usuariosPorGenero.put(genero, usuariosDelGenero);
                    }
                    usuariosDelGenero.put(usuario.getIdUsuario(), usuario);  // Hash como Set: key=id, value=usuario
                }
            }
        }
        
        // Y también guardo esta calificación para el director (para consultas futuras)
        if (pelicula != null && pelicula.getDirectorPrincipal() != null) {
            Director director = pelicula.getDirectorPrincipal();
            int calificacionesAnteriores = director.getCalificacionesDeSusPeliculas().size();
            director.getCalificacionesDeSusPeliculas().add(cal.getPuntuacion());  // acumulo todas las calificaciones de sus películas
            
            // Para consulta 4: Si el director alcanza los criterios (>1 película y >100 evaluaciones), agregarlo al índice
            if (calificacionesAnteriores <= 100 && 
                director.getCalificacionesDeSusPeliculas().size() > 100 &&
                director.getPeliculasDirigidas().size() > 1) {
                directoresCalificados.add(director);
            }
        }
    }

    // Métodos de búsqueda específicos
    public Pelicula getPeliculaPorId(String id) {
        return peliculasPorId.get(id);
    }

    public Usuario getUsuarioPorId(String id) {
        return usuariosPorId.get(id);
    }

    public Actor getActorPorId(String id) {
        return actoresPorId.get(id);
    }

    public Director getDirectorPorId(String id) {
        return directoresPorId.get(id);
    }

    public Genero getGeneroPorId(String id) {
        return generosPorId.get(id);
    }

    public Coleccion getColeccionPorId(String id) {
        return coleccionesPorId.get(id);
    }
    
    // Método para construir índices optimizados después de la carga completa de datos
    public void construirIndicesOptimizados() {
        // Limpiar índices existentes
        peliculasBienCalificadas = new MyLinkedListImpl<>();
        directoresCalificados = new MyLinkedListImpl<>();
        
        // Construir índice de películas con >100 calificaciones
        MyList<Pelicula> todasLasPeliculas = peliculasPorId.values();
        for (int i = 0; i < todasLasPeliculas.size(); i++) {
            Pelicula pelicula = todasLasPeliculas.get(i);
            if (pelicula.getTotalCalificaciones() > 100) {
                peliculasBienCalificadas.add(pelicula);
            }
        }
        
        // Construir índice de directores con >1 película y >100 evaluaciones
        MyList<Director> todosLosDirectores = directoresPorId.values();
        for (int i = 0; i < todosLosDirectores.size(); i++) {
            Director director = todosLosDirectores.get(i);
            if (director.getPeliculasDirigidas().size() > 1 && 
                director.getCalificacionesDeSusPeliculas().size() > 100) {
                directoresCalificados.add(director);
            }
        }
        
        System.out.println("Construcción de índices completada exitosamente!");
    }
        


    
    // Por si necesito calcular la calificación media de una película específica
    public double getCalificacionMedia(String idPelicula) {
        Pelicula pelicula = getPeliculaPorId(idPelicula);
        if (pelicula != null && pelicula.getTotalCalificaciones() > 0) {
            return pelicula.getSumaCalificaciones() / pelicula.getTotalCalificaciones();  // promedio simple
        }
        return 0.0;  // si no tiene calificaciones o no existe
    }
    
    public MyList<Pelicula> getPeliculas() {
        return peliculasPorId.values();
    }
    
    public MyList<Usuario> getUsuarios() {
        return usuariosPorId.values();
    }
    
    public MyList<Actor> getActores() {
        return actoresPorId.values();
    }
    
    public MyList<Director> getDirectores() {
        return directoresPorId.values();
    }
    
    public MyList<Genero> getGeneros() {
        return generosPorId.values();
    }
    
    public MyList<Coleccion> getColecciones() {
        return coleccionesPorId.values();
    }
}
