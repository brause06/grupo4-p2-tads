package uy.edu.um;

import uy.edu.um.clases.UMovieApp;
import uy.edu.um.clases.CargarDatos;
import uy.edu.um.clases.Pelicula;
import uy.edu.um.clases.Actor;
import uy.edu.um.clases.Director;
import uy.edu.um.clases.Genero;
import uy.edu.um.clases.Usuario;
import uy.edu.um.clases.Coleccion;
import uy.edu.um.tad.linkedlist.MyList;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;
import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;
import java.util.Scanner;

public class Main {
    // Las cosas principales que necesito para que funcione el programa
    private static UMovieApp app;               // aplicación principal donde está toda la lógica
    private static CargarDatos cargador;        // se encarga de leer los CSV
    private static Scanner scanner;             // para leer lo que escribe el usuario
    private static boolean datosCargados = false;  // para saber si ya cargué los datos o no

    public static void main(String[] args) {
        app = new UMovieApp();
        cargador = new CargarDatos(app);
        scanner = new Scanner(System.in);
        
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║              🎬 UMOVIE SYSTEM 🎬              ║");
        System.out.println("║    Sistema de Recomendación de Películas     ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        
        // Arranco el programa principal
        menuPrincipal();
        
        // Limpio y me despido
        scanner.close();
        System.out.println("\n¡Gracias por usar UMovie System! 👋");
    }
    
    private static void menuPrincipal() {
        int opcion;
        
        do {
            mostrarMenuPrincipal();
            opcion = leerOpcion();
            
            switch (opcion) {
                case 1:
                    cargarDatos();
                    break;
                case 2:
                    if (datosCargados) {
                        menuConsultas();  // si ya cargué los datos, puede hacer consultas
                    } else {
                        System.out.println("\n❌ Error: Debe cargar los datos primero (opción 1)");
                        System.out.println("   Presione ENTER para continuar...");
                        scanner.nextLine();
                    }
                    break;
                case 3:
                    System.out.println("\n👋 Saliendo del sistema...");
                    break;
                default:
                    System.out.println("\n❌ Opción inválida. Por favor, seleccione 1, 2 o 3.");
                    System.out.println("   Presione ENTER para continuar...");
                    scanner.nextLine();
            }
        } while (opcion != 3);
    }
    
    private static void mostrarMenuPrincipal() {
        System.out.println("Seleccione la opción que desee:");
        System.out.println("1. Carga de datos");
        System.out.println("2. Ejecutar consultas");
        System.out.println("3. Salir");
    }
    
    private static void cargarDatos() {
        // Estos son los archivos CSV que tengo que leer
        String rutaMoviesMetadata = "movies_metadata.csv";   // info básica de películas
        String rutaCredits = "credits.csv";                  // actores y directores
        String rutaRatings = "ratings_1mm.csv";              // calificaciones de usuarios
        
        long startTimeTotal = System.currentTimeMillis();   // empiezo a medir el tiempo
        
        try {
            cargador.cargarPeliculas(rutaMoviesMetadata);
            cargador.cargarCreditos(rutaCredits);
            cargador.cargarCalificaciones(rutaRatings);
            
            // Construir índices optimizados después de la carga completa
            app.construirIndicesOptimizados();
            
            long endTimeTotal = System.currentTimeMillis();
            
            datosCargados = true;  // marco que ya terminé de cargar
            System.out.println("Carga de datos exitosa, tiempo de ejecución de la carga: " + (endTimeTotal - startTimeTotal) + " ms");
            
        } catch (Exception e) {
            System.err.println("Error durante la carga: " + e.getMessage());
            datosCargados = false;  // si algo falló, marco que no están cargados
        }
    }
    
    private static void menuConsultas() {
        int opcion;
        
        do {
            mostrarMenuConsultas();
            opcion = leerOpcion();
            
            switch (opcion) {
                case 1:
                    ejecutarConsulta1();
                    break;
                case 2:
                    ejecutarConsulta2();
                    break;
                case 3:
                    ejecutarConsulta3();
                    break;
                case 4:
                    ejecutarConsulta4();
                    break;
                case 5:
                    ejecutarConsulta5();
                    break;
                case 6:
                    ejecutarConsulta6();
                    break;
                case 7:
                    // Salir del menú de consultas - vuelve al menú principal
                    break;
                default:
                    System.out.println("\n❌ Opción inválida. Por favor, seleccione entre 1 y 7.");
                    System.out.println("   Presione ENTER para continuar...");
                    scanner.nextLine();
            }
        } while (opcion != 7);
    }
    
    private static void mostrarMenuConsultas() {
        System.out.println("1. Top 5 de las películas que más calificaciones por idioma.");
        System.out.println("2. Top 10 de las películas que mejor calificación media tienen por parte de los usuarios.");
        System.out.println("3. Top 5 de las colecciones que más ingresos generaron.");
        System.out.println("4. Top 10 de los directores que mejor calificación tienen.");
        System.out.println("5. Actor con más calificaciones recibidas en cada mes del año.");
        System.out.println("6. Usuarios con más calificaciones por género");
        System.out.println("7. Salir");
    }
    
    // Para leer lo que escribe el usuario sin que se rompa si mete letras en lugar de números
    private static int leerOpcion() {
        try {
            String input = scanner.nextLine();
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return -1; // si no es un número válido, devuelvo -1 (que va a ser "opción inválida")
        }
    }
    


    // CONSULTAS
    //Consulta 1 ********************************************************
    private static void ejecutarConsulta1() {
        
        String[][] idiomas = {
            {"en", "Inglés"},
            {"fr", "Francés"}, 
            {"it", "Italiano"},
            {"es", "Español"},
            {"pt", "Portugués"}
        };
        
        long startTime = System.currentTimeMillis();
        
        // Obtener el índice de películas por idioma
        MyHash<String, MyList<Pelicula>> peliculasPorIdioma = app.getPeliculasPorIdioma();
        
        for (String[] idioma : idiomas) {
            String codigoIdioma = idioma[0];
            String nombreIdioma = idioma[1];
            
            // Crear heap para encontrar top 5 películas de este idioma por calificaciones
            MyHeap<Pelicula> heapPeliculas = new MyHeapImpl<>(false); 
            
            // Obtener directamente las películas de este idioma (sin iterar todas)
            MyList<Pelicula> peliculasDelIdioma = peliculasPorIdioma.get(codigoIdioma);
            
            if (peliculasDelIdioma != null) {
                // Solo iterar las películas de este idioma específico
                for (int i = 0; i < peliculasDelIdioma.size(); i++) {
                    Pelicula pelicula = peliculasDelIdioma.get(i);
                    if (pelicula.getTotalCalificaciones() > 0) {
                        heapPeliculas.insert(pelicula);
                    }
                }
            }
            
            // Extraer top 5 del heap
            int maxPeliculas = Math.min(5, heapPeliculas.size());
            for (int i = 0; i < maxPeliculas; i++) {
                Pelicula pelicula = heapPeliculas.delete();
                
                System.out.printf("%s, %s, %d, %s\n", 
                    pelicula.getIdPelicula(),
                    pelicula.getTitulo(),
                    pelicula.getTotalCalificaciones(),
                    codigoIdioma);
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.printf("Tiempo de ejecución de la consulta: %d ms\n", (endTime - startTime));
    }

    //Consulta 2 ********************************************************
    private static void ejecutarConsulta2() {
        
        long startTime = System.currentTimeMillis();
        
        // Configurar heap para comparar por calificación media
        Pelicula.setCompararPorMedia(true);
        
        // Crear heap para encontrar top 10 películas por calificación media
        MyHeap<Pelicula> heapPeliculas = new MyHeapImpl<>(false); // Max heap
        
        // Obtener directamente las películas con >100 calificaciones
        MyList<Pelicula> peliculasBienCalificadas = app.getPeliculasBienCalificadas();
        
        // Agregar solo las películas bien calificadas al heap
        for (int i = 0; i < peliculasBienCalificadas.size(); i++) {
            Pelicula pelicula = peliculasBienCalificadas.get(i);
            heapPeliculas.insert(pelicula);
        }
        
        // Extraer top 10 del heap
        int maxPeliculas = Math.min(10, heapPeliculas.size());
        for (int i = 0; i < maxPeliculas; i++) {
            Pelicula pelicula = heapPeliculas.delete();
            double calificacionMedia = pelicula.getSumaCalificaciones() / pelicula.getTotalCalificaciones();
            
            System.out.printf("%s, %s, %.2f\n", 
                pelicula.getIdPelicula(),
                pelicula.getTitulo(),
                calificacionMedia);
        }
        
        // Restaurar el comparador por defecto
        Pelicula.setCompararPorMedia(false);
        
        long endTime = System.currentTimeMillis();
        
        System.out.printf("Tiempo de ejecución de la consulta: %d ms\n", (endTime - startTime));
    }

    //Consulta 3 ********************************************************
    private static void ejecutarConsulta3() {
        long startTime = System.currentTimeMillis();
        
        // Crear heap para encontrar top 5 colecciones por ingresos
        MyHeap<Coleccion> heapColecciones = new MyHeapImpl<>(false); // Max heap
        
        // Obtener directamente las colecciones con ingresos >0 
        MyList<Coleccion> coleccionesConIngresos = app.getColeccionesConIngresos();
        
        // Agregar solo las colecciones con ingresos al heap
        for (int i = 0; i < coleccionesConIngresos.size(); i++) {
            Coleccion coleccion = coleccionesConIngresos.get(i);
            heapColecciones.insert(coleccion);
        }
        
        // Extraer top 5 del heap
        int maxColecciones = Math.min(5, heapColecciones.size());
        for (int i = 0; i < maxColecciones; i++) {
            Coleccion coleccion = heapColecciones.delete();
            
            // Construir lista de IDs de películas en formato [id1,id2,...]
            StringBuilder idsPeliculas = new StringBuilder("[");
            for (int j = 0; j < coleccion.getPeliculas().size(); j++) {
                if (j > 0) idsPeliculas.append(",");
                idsPeliculas.append(coleccion.getPeliculas().get(j).getIdPelicula());
            }
            idsPeliculas.append("]");
            
            System.out.printf("%s,%s,%d,%s,%d\n", 
                coleccion.getIdColeccion(),
                coleccion.getTituloColeccion(),
                coleccion.getPeliculas().size(),
                idsPeliculas.toString(),
                coleccion.getIngresosTotales());
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("Tiempo de ejecución de la consulta: %d ms\n", (endTime - startTime));
    }
    
    //Consulta 4 ********************************************************
    private static void ejecutarConsulta4() {
        long startTime = System.currentTimeMillis();
        
        // Crear heap para encontrar top 10 directores por mediana de calificaciones
        MyHeap<Director> heapDirectores = new MyHeapImpl<>(false); // Max heap
        
        // Obtener directamente los directores con >1 película y >100 evaluaciones 
        MyList<Director> directoresCalificados = app.getDirectoresCalificados();
        
        // Agregar solo los directores calificados al heap
        for (int i = 0; i < directoresCalificados.size(); i++) {
            Director director = directoresCalificados.get(i);
            heapDirectores.insert(director);
        }
        
        // Extraer top 10 del heap
        int maxDirectores = Math.min(10, heapDirectores.size());
        for (int i = 0; i < maxDirectores; i++) {
            Director director = heapDirectores.delete();
            
            String nombreDirector = director.getNombreDirector();
            int cantidadPeliculas = director.getPeliculasDirigidas().size();
            double mediana = director.getMediana();
            
            System.out.printf("%s, %d, %.2f\n", 
                nombreDirector,
                cantidadPeliculas,
                mediana);
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("Tiempo de ejecución de la consulta: %d ms\n", (endTime - startTime));
    }
    
    //Consulta 5 ********************************************************
    private static void ejecutarConsulta5() {
        long startTime = System.currentTimeMillis();
        
        // Obtener el índice de actores por mes
        MyHash<Integer, MyHash<String, Actor>> actoresPorMes = app.getActoresPorMes();
        
        // Procesa cada mes por separado 
        for (int mes = 1; mes <= 12; mes++) {
            
            // Obtener directamente los actores activos en este mes
            MyHash<String, Actor> actoresDelMes = actoresPorMes.get(mes);
            
            if (actoresDelMes != null && actoresDelMes.size() > 0) {
                
                // Buscar el actor con más calificaciones en este mes específico
                Actor mejorActor = null;
                int maxCalificaciones = 0;
                
                MyList<Actor> listaActores = actoresDelMes.values();
                for (int i = 0; i < listaActores.size(); i++) {
                    Actor actor = listaActores.get(i);
                    Integer calificaciones = actor.getCalificacionesRecibidasPorMes().get(mes);
                    
                    if (calificaciones != null && calificaciones > maxCalificaciones) {
                        maxCalificaciones = calificaciones;
                        mejorActor = actor;
                    }
                }
                
                if (mejorActor != null) {
                    int cantidadPeliculas = mejorActor.getCantidadPeliculasEnMes(mes);
                    
                    System.out.printf("%d, %s, %d, %d\n", 
                        mes,
                        mejorActor.getNombreActor(),
                        cantidadPeliculas,
                        maxCalificaciones);
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("Tiempo de ejecución de la consulta: %d ms\n", (endTime - startTime));
    }
    
    //Consulta 6 ********************************************************
    private static void ejecutarConsulta6() {
        long startTime = System.currentTimeMillis();
        
        // PASO 1: Obtener top 10 géneros usando heap 
        MyHeap<Genero> heapGeneros = new MyHeapImpl<>(false); // Max heap
        MyList<Genero> todosLosGeneros = app.getGeneros();
        
        // Agregar todos los géneros con calificaciones al heap
        for (int i = 0; i < todosLosGeneros.size(); i++) {
            Genero genero = todosLosGeneros.get(i);
            if (genero.getTotalCalificacionesRecibidas() > 0) {
                heapGeneros.insert(genero);
            }
        }
        
        // Extraer top 10 géneros del heap
        MyList<Genero> top10Generos = new MyLinkedListImpl<>();
        int maxGeneros = Math.min(10, heapGeneros.size());
        
        for (int i = 0; i < maxGeneros; i++) {
            Genero genero = heapGeneros.delete();
            top10Generos.add(genero);
        }
        
        // PASO 2
        // Crear hashes para mantener el mejor usuario por cada género del top 10 
        MyHash<Integer, Usuario> mejoresUsuarios = new MyHashImpl<>();
        MyHash<Integer, Integer> maxCalificacionesPorGenero = new MyHashImpl<>();
        
        // Inicializar con valores por defecto
        for (int i = 0; i < top10Generos.size(); i++) {
            maxCalificacionesPorGenero.put(i, 0);
        }
        
        MyList<Usuario> todosLosUsuarios = app.getUsuarios();
        for (int i = 0; i < todosLosUsuarios.size(); i++) {
            Usuario usuario = todosLosUsuarios.get(i);
            
            // Para este usuario, revisar si es el mejor en algún género del top 10
            for (int g = 0; g < top10Generos.size(); g++) {
                Genero genero = top10Generos.get(g);
                Integer calificaciones = usuario.getCalificacionesPorGenero().get(genero);
                
                if (calificaciones != null && calificaciones > maxCalificacionesPorGenero.get(g)) {
                    maxCalificacionesPorGenero.put(g, calificaciones);
                    mejoresUsuarios.put(g, usuario);
                }
            }
        }
        
        // Imprimir resultados
        for (int g = 0; g < top10Generos.size(); g++) {
            Usuario mejorUsuario = mejoresUsuarios.get(g);
            if (mejorUsuario != null) {
                System.out.printf("%s,%s,%d\n", 
                    mejorUsuario.getIdUsuario(),
                    top10Generos.get(g).getNombreGenero(),
                    maxCalificacionesPorGenero.get(g));
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.printf("Tiempo de ejecución de la consulta: %d ms\n", (endTime - startTime));
    }
}