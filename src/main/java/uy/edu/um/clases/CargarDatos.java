package uy.edu.um.clases;

import lombok.AllArgsConstructor;
import uy.edu.um.tad.linkedlist.MyLinkedListImpl;
import uy.edu.um.tad.linkedlist.MyList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@AllArgsConstructor
public class CargarDatos {
    private final UMovieApp app; 

    // Este metodo lee movies_metadata.csv y carga todas las películas con sus géneros y colecciones
    public void cargarPeliculas(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            // Primero leo la línea de encabezados (que a veces puede tener saltos de línea dentro)
            String encabezado = leerRegistroCompleto(br);
            System.out.println("Encabezado procesado correctamente");
            
            String registroCompleto;
            int contador = 0;  // para saber cuántas películas voy procesando
            
            while ((registroCompleto = leerRegistroCompleto(br)) != null) {
                MyList<String> campos = parseCSVMejorado(registroCompleto);
                
                // Me aseguro de que la línea tenga todos los campos que necesito
                if (campos.size() < 19) {
                    System.err.println("Linea con campos insuficientes: " + campos.size());
                    continue;
                }

                try {
                    // Extraigo los datos básicos de la película
                    String id = limpiarCampo(campos.get(5));
                    if (id.isEmpty()) continue;  // si no tiene ID la ignoro

                    String titulo = limpiarCampo(campos.get(8));      // original_title
                    String idioma = limpiarCampo(campos.get(7));      // original_language  

                    long revenue = parseLongSeguro(campos.get(13));   // ingresos

                    LocalDate releaseDate = parseFecha(campos.get(12)); // fecha de estreno (solo para extraer mes)

                    // Crear película con constructor optimizado
                    Pelicula pelicula = new Pelicula(id, titulo, idioma, revenue, releaseDate);

                    // Ahora me fijo si esta película pertenece a alguna colección
                    String coleccionStr = campos.get(1);
                    String idColeccion = parseCampoJson(coleccionStr, "id");
                    String nombreColeccion = parseCampoJson(coleccionStr, "name");

                    if (idColeccion != null && nombreColeccion != null) {
                        // Busco si ya existe esta colección, si no la creo
                        Coleccion coleccion = app.getColeccionPorId(idColeccion);
                        if (coleccion == null) {
                            coleccion = new Coleccion(idColeccion, nombreColeccion);
                            app.agregarColeccion(coleccion);
                        }
                        // Agrego esta película a la colección y viceversa
                        coleccion.getPeliculas().add(pelicula);
                        pelicula.setColeccion(coleccion);
                    }

                    // También proceso los géneros de esta película
                    String generosStr = campos.get(3);
                    MyList<Genero> generos = parseGeneros(generosStr);
                    
                    for (int i = 0; i < generos.size(); i++) {
                        Genero g = generos.get(i);

                        // Si ya existe este género, uso el existente, si no lo creo
                        Genero existente = app.getGeneroPorId(g.getIdGenero());
                        if (existente == null) {
                            app.agregarGenero(g);
                            pelicula.getGeneros().add(g);
                        } else {
                            pelicula.getGeneros().add(existente);
                        }
                    }

                    // Finalmente agrego la película completa a la aplicación
                    app.agregarPelicula(pelicula);
                    contador++;
                    
                    // Cada 1000 películas muestro el progreso para saber que está funcionando
                    if (contador % 1000 == 0) {
                        System.out.println("Procesados: " + contador + " registros");
                    }

                } catch (Exception ex) {
                    System.err.println("Error procesando registro " + contador + ": " + ex.getMessage());
                }
            }
            
            System.out.println("Total registros procesados: " + contador);

        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
    }
    
    private String leerRegistroCompleto(BufferedReader br) throws IOException {
        StringBuilder registro = new StringBuilder();
        String linea;
        int comillasAbiertas = 0;
        
        while ((linea = br.readLine()) != null) {
            // Cuento las comillas para saber si estoy dentro de un campo o no
            for (int i = 0; i < linea.length(); i++) {
                if (linea.charAt(i) == '"') {
                    comillasAbiertas++;
                }
            }
            
            registro.append(linea);
            
            // Si hay un número par de comillas, significa que el registro está completo
            if (comillasAbiertas % 2 == 0) {
                break;
            } else {
                // Si no, significa que el campo continúa en la siguiente línea
                registro.append("\n");
            }
        }
        
        return registro.length() > 0 ? registro.toString() : null;
    }
    
    // Este metodo separa correctamente los campos del CSV respetando las comillas
    private MyList<String> parseCSVMejorado(String linea) {
        MyList<String> campos = new MyLinkedListImpl<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroDeComillas = false;
        
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            
            if (c == '"') {
                // Si encuentro dos comillas seguidas dentro de un campo, es una comilla literal
                if (i + 1 < linea.length() && linea.charAt(i + 1) == '"' && dentroDeComillas) {
                    actual.append('"');
                    i++; // me salto la siguiente comilla
                } else {
                    dentroDeComillas = !dentroDeComillas;  // cambio el estado
                }
            } else if (c == ',' && !dentroDeComillas) {
                // Solo separo por coma si no estoy dentro de comillas
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        
        // Agregar el último campo
        campos.add(actual.toString());
        
        return campos;  // devuelvo directamente la MyList
    }
    
    // Para limpiar los campos y que no queden comillas o espacios extra
    private String limpiarCampo(String campo) {
        if (campo == null) return "";
        
        String limpio = campo.trim();
        
        // Si el campo está rodeado de comillas las saco
        if (limpio.startsWith("\"") && limpio.endsWith("\"") && limpio.length() > 1) {
            limpio = limpio.substring(1, limpio.length() - 1);
        }
        
        return limpio.trim();
    }
    
    private long parseLongSeguro(String valor) {
        try {
            String limpio = limpiarCampo(valor);
            return limpio.isEmpty() ? 0 : Long.parseLong(limpio);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private double parseDoubleSeguro(String valor) {
        try {
            String limpio = limpiarCampo(valor);
            return limpio.isEmpty() ? 0.0 : Double.parseDouble(limpio);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private LocalDate parseFecha(String valor) {
        try {
            String limpio = limpiarCampo(valor);
            return limpio.isEmpty() ? null : LocalDate.parse(limpio);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
    
    private String parseCampoJson(String json, String campo) {
        if (json == null || json.isEmpty() || json.equals("null")) return null;
        
        String jsonLimpio = limpiarCampo(json);
        
        // Buscar el campo (tanto con comillas simples como dobles)
        int idx = jsonLimpio.indexOf("'" + campo + "'");
        if (idx == -1) idx = jsonLimpio.indexOf("\"" + campo + "\"");
        if (idx == -1) return null;
        
        // Buscar el valor después de los dos puntos
        int start = jsonLimpio.indexOf(":", idx);
        if (start == -1) return null;
        start++; // Saltar los dos puntos
        
        // Saltar espacios en blanco
        while (start < jsonLimpio.length() && Character.isWhitespace(jsonLimpio.charAt(start))) {
            start++;
        }
        
        int end;
        char firstChar = jsonLimpio.charAt(start);
        
        if (firstChar == '\'' || firstChar == '"') {
            // Es un string, buscar la comilla de cierre del mismo tipo
            start++; // Saltar comilla de apertura
            end = jsonLimpio.indexOf(String.valueOf(firstChar), start);
            if (end == -1) return null;
            return jsonLimpio.substring(start, end);
        } else {
            // Es un número, buscar coma o llave de cierre
            end = jsonLimpio.indexOf(",", start);
            if (end == -1) end = jsonLimpio.indexOf("}", start);
            if (end == -1) return null;
            
            String valor = jsonLimpio.substring(start, end).trim();
            return valor;
        }
    }
    
    private MyList<Genero> parseGeneros(String json) {
        MyList<Genero> lista = new MyLinkedListImpl<>();
        if (json == null || json.isEmpty() || json.equals("[]")) return lista;

        String jsonLimpio = limpiarCampo(json);
        
        // Remover corchetes externos si existen
        if (jsonLimpio.startsWith("[") && jsonLimpio.endsWith("]")) {
            jsonLimpio = jsonLimpio.substring(1, jsonLimpio.length() - 1);
        }
        
        // Separar objetos individuales
        String[] objetos = separarObjetosJson(jsonLimpio);
        
        for (String objeto : objetos) {
            if (objeto.trim().isEmpty()) continue;
            
            // Asegurar que el objeto tenga llaves
            String objetoCompleto = objeto.trim();
            if (!objetoCompleto.startsWith("{")) {
                objetoCompleto = "{" + objetoCompleto;
            }
            if (!objetoCompleto.endsWith("}")) {
                objetoCompleto = objetoCompleto + "}";
            }
            
            String id = parseCampoJson(objetoCompleto, "id");
            String name = parseCampoJson(objetoCompleto, "name");
            
            if (id != null && name != null) {
                lista.add(new Genero(id, name, 0));
            }
        }
        return lista;
    }
    
    // Metodo auxiliar para separar objetos JSON en un array
    private String[] separarObjetosJson(String contenido) {
        MyList<String> objetos = new MyLinkedListImpl<>();
        StringBuilder objetoActual = new StringBuilder();
        int nivelLlaves = 0;
        boolean dentroDeComillas = false;
        char tipoComilla = 0;
        
        for (int i = 0; i < contenido.length(); i++) {
            char c = contenido.charAt(i);
            
            if ((c == '\'' || c == '"') && (i == 0 || contenido.charAt(i-1) != '\\')) {
                if (!dentroDeComillas) {
                    dentroDeComillas = true;
                    tipoComilla = c;
                } else if (c == tipoComilla) {
                    dentroDeComillas = false;
                }
            }
            
            if (!dentroDeComillas) {
                if (c == '{') {
                    nivelLlaves++;
                } else if (c == '}') {
                    nivelLlaves--;
                }
            }
            
            objetoActual.append(c);
            
            // Si cerramos un objeto completo y encontramos una coma, o llegamos al final
            if (nivelLlaves == 0 && objetoActual.length() > 0) {
                // Buscar la próxima coma fuera de comillas
                int j = i + 1;
                boolean encontrarComa = false;
                while (j < contenido.length() && Character.isWhitespace(contenido.charAt(j))) {
                    j++;
                }
                if (j < contenido.length() && contenido.charAt(j) == ',') {
                    encontrarComa = true;
                    i = j; // Saltar la coma
                }
                
                objetos.add(objetoActual.toString().trim());
                objetoActual.setLength(0);
            }
        }
        
        // Agregar el último objeto si queda algo
        if (objetoActual.length() > 0) {
            objetos.add(objetoActual.toString().trim());
        }
        
        String[] resultado = new String[objetos.size()];
        for (int i = 0; i < objetos.size(); i++) {
            resultado[i] = objetos.get(i);
        }
        
        return resultado;
    }

    // Metodo de prueba para validar la carga
    public void mostrarEstadisticasCarga() {
        System.out.println("=== ESTADÍSTICAS DE CARGA ===");
        System.out.println("Total películas cargadas: " + app.getPeliculas().size());
        System.out.println("Total géneros cargados: " + app.getGeneros().size());
        System.out.println("Total colecciones cargadas: " + app.getColecciones().size());
        System.out.println("Total usuarios cargados: " + app.getUsuarios().size());
        System.out.println("Total actores cargados: " + app.getActores().size());
        System.out.println("Total directores cargados: " + app.getDirectores().size());
        System.out.println("Total calificaciones cargadas: " + app.getCalificaciones().size());
        
        // Mostrar algunas películas de ejemplo
        System.out.println("\n=== PRIMERAS 3 PELÍCULAS ===");
        for (int i = 0; i < Math.min(3, app.getPeliculas().size()); i++) {
            Pelicula p = app.getPeliculas().get(i);
            System.out.println("ID: " + p.getIdPelicula() + 
                             " | Título: " + p.getTitulo() + 
                             " | Géneros: " + p.getGeneros().size() +
                             " | Ingresos: $" + p.getIngresos() +
                             " | Calificaciones: " + p.getTotalCalificaciones());
        }
    }
    
    // Este metodo lee ratings_1mm.csv y carga todas las calificaciones
    // Formato del CSV: userId,movieId,rating,timestamp
    public void cargarCalificaciones(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea = br.readLine(); // me salto la primera línea (encabezados)
            System.out.println("Encabezado calificaciones: " + linea);
            
            int contador = 0;      // calificaciones procesadas exitosamente
            int ignoradas = 0;     // calificaciones que no pude procesar
            
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split(",");
                
                // Me aseguro de que la línea tenga todos los campos necesarios
                if (campos.length < 4) {
                    ignoradas++;
                    continue;
                }
                
                try {
                    // Extraigo los datos de la calificación
                    String idUsuario = campos[0].trim();
                    String idPelicula = campos[1].trim();
                    double puntuacion = Double.parseDouble(campos[2].trim());
                    long timestamp = Long.parseLong(campos[3].trim());
                    
                    LocalDateTime fechaHora = java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(timestamp), 
                        java.time.ZoneId.systemDefault()
                    );
                    int mes = fechaHora.getMonthValue(); // Solo extraer el mes (1-12)
                    
                    // Solo proceso la calificación si la película existe en mi sistema
                    if (app.getPeliculaPorId(idPelicula) != null) {
                        // Si es la primera vez que veo este usuario, lo creo
                        Usuario usuario = app.getUsuarioPorId(idUsuario);
                        if (usuario == null) {
                            usuario = new Usuario(idUsuario);
                            app.agregarUsuario(usuario);
                        }
                        
                        Calificacion calificacion = new Calificacion(idUsuario, idPelicula, puntuacion, fechaHora);
                        app.agregarCalificacion(calificacion);
                        contador++;
                        
                        // Cada 10000 calificaciones muestro el progreso
                        if (contador % 10000 == 0) {
                            System.out.println("Calificaciones procesadas: " + contador);
                        }
                    } else {
                        ignoradas++;  // si la película no existe, ignoro esta calificación
                    }
                    
                } catch (Exception ex) {
                    System.err.println("Error procesando calificación línea " + contador + ": " + ex.getMessage());
                    ignoradas++;
                }
            }
            
            System.out.println("Total calificaciones procesadas: " + contador);
            System.out.println("Calificaciones ignoradas (película no encontrada o error): " + ignoradas);
            
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de calificaciones: " + e.getMessage());
        }
    }
    
    // Este metodo lee credits.csv y agrega actores y directores a las películas
    // El CSV tiene campos JSON complicados con info de cast y crew
    public void cargarCreditos(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            // Leo la línea de encabezados (que puede tener saltos de línea)
            String encabezado = leerRegistroCompleto(br);
            System.out.println("Encabezado créditos procesado correctamente");
            
            String registroCompleto;
            int contador = 0;  // películas procesadas
            
            while ((registroCompleto = leerRegistroCompleto(br)) != null) {
                MyList<String> campos = parseCSVMejorado(registroCompleto);
                
                if (campos.size() < 3) {
                    System.err.println("Línea de créditos con campos insuficientes: " + campos.size());
                    continue;
                }
                
                try {
                    String idPelicula = limpiarCampo(campos.get(2)); // el ID de película está en la columna 2
                    Pelicula pelicula = app.getPeliculaPorId(idPelicula);
                    
                    if (pelicula == null) {
                        continue; // si la película no existe la ignoro
                    }
                    
                    // Proceso los actores (están en formato JSON en el campo 0)
                    String castStr = campos.get(0);
                    procesarActores(castStr, pelicula);
                    
                    // Proceso el crew para encontrar directores (campo 1)  
                    String crewStr = campos.get(1);
                    procesarDirectores(crewStr, pelicula);
                    
                    contador++;
                    
                    // Cada 1000 películas muestro el progreso
                    if (contador % 1000 == 0) {
                        System.out.println("Créditos procesados: " + contador + " películas");
                    }
                    
                } catch (Exception ex) {
                    System.err.println("Error procesando créditos " + contador + ": " + ex.getMessage());
                }
            }
            
            System.out.println("Total créditos procesados: " + contador);
            
        } catch (IOException e) {
            System.out.println("Error al leer el archivo de créditos: " + e.getMessage());
        }
    }
    
    // Este metodo parsea el JSON de actores y los conecta con la película
    private void procesarActores(String castJson, Pelicula pelicula) {
        if (castJson == null || castJson.isEmpty() || castJson.equals("[]")) return;  // si no hay actores no hago nada
        
        String jsonLimpio = limpiarCampo(castJson);
        
        // Saco los corchetes externos del array JSON
        if (jsonLimpio.startsWith("[") && jsonLimpio.endsWith("]")) {
            jsonLimpio = jsonLimpio.substring(1, jsonLimpio.length() - 1);
        }
        
        String[] objetos = separarObjetosJson(jsonLimpio);
        
        for (String objeto : objetos) {
            if (objeto.trim().isEmpty()) continue;
            
            String objetoCompleto = objeto.trim();
            if (!objetoCompleto.startsWith("{")) {
                objetoCompleto = "{" + objetoCompleto;
            }
            if (!objetoCompleto.endsWith("}")) {
                objetoCompleto = objetoCompleto + "}";
            }
            
            String idActor = parseCampoJson(objetoCompleto, "id");
            String nombreActor = parseCampoJson(objetoCompleto, "name");
            
            if (idActor != null && nombreActor != null) {
                // Si ya existe este actor lo busco, si no lo creo
                Actor actor = app.getActorPorId(idActor);
                if (actor == null) {
                    actor = new Actor(idActor, nombreActor);
                    app.agregarActor(actor);
                }
                
                // Conecto el actor con la película en ambas direcciones
                if (!pelicula.getActoresParticipantes().contains(actor)) {
                    pelicula.getActoresParticipantes().add(actor);
                }
                if (!actor.getPeliculasEnLasQueActua().contains(pelicula)) {
                    actor.getPeliculasEnLasQueActua().add(pelicula);
                }
            }
        }
    }
    

     //Procesa el JSON de crew y extrae los directores
    private void procesarDirectores(String crewJson, Pelicula pelicula) {
        if (crewJson == null || crewJson.isEmpty() || crewJson.equals("[]")) return;
        
        String jsonLimpio = limpiarCampo(crewJson);
        
        // Remover corchetes externos
        if (jsonLimpio.startsWith("[") && jsonLimpio.endsWith("]")) {
            jsonLimpio = jsonLimpio.substring(1, jsonLimpio.length() - 1);
        }
        
        String[] objetos = separarObjetosJson(jsonLimpio);
        
        for (String objeto : objetos) {
            if (objeto.trim().isEmpty()) continue;
            
            String objetoCompleto = objeto.trim();
            if (!objetoCompleto.startsWith("{")) {
                objetoCompleto = "{" + objetoCompleto;
            }
            if (!objetoCompleto.endsWith("}")) {
                objetoCompleto = objetoCompleto + "}";
            }
            
            String job = parseCampoJson(objetoCompleto, "job");
            
            // Solo procesamos directores
            if ("Director".equals(job)) {
                String idDirector = parseCampoJson(objetoCompleto, "id");
                String nombreDirector = parseCampoJson(objetoCompleto, "name");
                
                if (idDirector != null && nombreDirector != null) {
                    Director director = app.getDirectorPorId(idDirector);
                    if (director == null) {
                        director = new Director(idDirector, nombreDirector);
                        app.agregarDirector(director);
                    }
                    
                    // Asignar como director principal (tomamos el primero que encontremos)
                    if (pelicula.getDirectorPrincipal() == null) {
                        pelicula.setDirectorPrincipal(director);
                        director.getPeliculasDirigidas().add(pelicula);
                    }
                }
            }
        }
    }
}
