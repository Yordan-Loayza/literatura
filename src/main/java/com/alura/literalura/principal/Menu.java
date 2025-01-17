package com.alura.literalura.principal;

import com.alura.literalura.dto.AutorDTO;
import com.alura.literalura.dto.LibroDTO;
import com.alura.literalura.dto.RespuestaLibrosDTO;
import com.alura.literalura.model.Autor;
import com.alura.literalura.model.Libro;
import com.alura.literalura.service.AutorService;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;
import com.alura.literalura.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Clase principal que maneja el menú de interacción con el usuario.
 * Esta clase proporciona opciones para buscar libros, listar libros registrados, listar autores registrados,
 * listar autores vivos en un año específico y listar libros por idioma.
 */
@Component
public class Menu {

    @Autowired
    private LibroService libroService;

    @Autowired
    private AutorService autorService;

    @Autowired
    private ConsumoAPI consumoAPI;

    @Autowired
    private ConvierteDatos convierteDatos;

    private static final String BASE_URL = "https://gutendex.com/books/";

    /**
     * Muestra el menú principal y maneja las opciones seleccionadas por el usuario.
     */
    public void mostrarMenu() {
        Scanner scanner = new Scanner(System.in);
        int opcion=-1;

        do {
            mostrarOpcionesMenu();
            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea

                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo(scanner);
                        break;

                    case 2:
                        listarLibrosRegistrados();
                        break;

                    case 3:
                        listarAutoresRegistrados();
                        break;

                    case 4:
                        listarAutoresVivosEnAno(scanner);
                        break;

                    case 5:
                        listarLibrosPorIdioma(scanner);
                        break;

                    case 0:
                        System.out.println("Saliendo...");
                        break;

                    default:
                        System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (Exception e) {
                System.out.println("Error en la opción seleccionada: " + e.getMessage());
                scanner.nextLine(); // Limpiar buffer
            }
        } while (opcion != 0);

        scanner.close();
    }

    private void mostrarOpcionesMenu() {
        System.out.println("--- LITERALURA ---");
        System.out.println("1 - Buscar libro por título");
        System.out.println("2 - Listar libros registrados");
        System.out.println("3 - Listar autores registrados");
        System.out.println("4 - Listar autores vivos en un año");
        System.out.println("5 - Listar libros por idioma");
        System.out.println("0 - Salir");
        System.out.print("Seleccione una opción: ");
    }

    private void buscarLibroPorTitulo(Scanner scanner) {
        System.out.print("Ingrese el título del libro: ");
        String titulo = scanner.nextLine();

        try {
            String encodedTitulo = URLEncoder.encode(titulo, StandardCharsets.UTF_8);
            String json = consumoAPI.obtenerDatos(BASE_URL + "?search=" + encodedTitulo);

            RespuestaLibrosDTO respuestaLibrosDTO = convierteDatos.obtenerDatos(json, RespuestaLibrosDTO.class);
            List<LibroDTO> librosDTO = respuestaLibrosDTO.getLibros();

            if (librosDTO.isEmpty()) {
                System.out.println("No se encontró ningún libro en la API.");
                return;
            }

            procesarLibrosEncontrados(librosDTO, titulo);
        } catch (Exception e) {
            System.out.println("Error al obtener datos de la API: " + e.getMessage());
        }
    }

    private void procesarLibrosEncontrados(List<LibroDTO> librosDTO, String titulo) {
        boolean libroRegistrado = false;

        for (LibroDTO libroDTO : librosDTO) {
            if (libroDTO.getTitulo().toLowerCase().contains(titulo.toLowerCase())) {
                Optional<Libro> libroExistente = libroService.obtenerLibroPorTitulo(libroDTO.getTitulo());
                if (libroExistente.isPresent()) {
                    System.out.println("El libro '" + libroDTO.getTitulo() + "' ya está registrado.");
                } else {
                    registrarNuevoLibro(libroDTO);
                }
                libroRegistrado = true;
                break;  // Salir del ciclo una vez que se haya procesado el libro
            }
        }

        if (!libroRegistrado) {
            System.out.println("No se encontró un libro que contenga '" + titulo + "' en la API.");
        }
    }

    private void registrarNuevoLibro(LibroDTO libroDTO) {
        Libro libro = new Libro();
        libro.setTitulo(libroDTO.getTitulo());
        libro.setIdioma(libroDTO.getIdiomas().get(0)); // Asumimos que el primer idioma es el principal
        libro.setNumeroDescargas(libroDTO.getNumeroDescargas());

        AutorDTO autorDTO = libroDTO.getAutores().get(0);
        Autor autor = autorService.obtenerAutorPorNombre(autorDTO.getNombre())
                .orElseGet(() -> crearNuevoAutor(autorDTO));

        libro.setAutor(autor);
        libroService.crearLibro(libro);

        System.out.println("Libro registrado: " + libro.getTitulo());
        mostrarDetallesLibro(libroDTO);
    }

    private Autor crearNuevoAutor(AutorDTO autorDTO) {
        Autor nuevoAutor = new Autor();
        nuevoAutor.setNombre(autorDTO.getNombre());
        nuevoAutor.setAnoNacimiento(autorDTO.getAnoNacimiento());
        nuevoAutor.setAnoFallecimiento(autorDTO.getAnoFallecimiento());
        return autorService.crearAutor(nuevoAutor);
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = libroService.listarLibros();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            libros.forEach(libro -> {
                System.out.println("------LIBRO--------");
                System.out.println("Título: " + libro.getTitulo());
                System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
                System.out.println("Idioma: " + libro.getIdioma());
                System.out.println("Número de descargas: " + libro.getNumeroDescargas());
            });
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorService.listarAutores();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
        } else {
            autores.forEach(autor -> {
                System.out.println("-------AUTOR-------");
                System.out.println("Autor: " + autor.getNombre());
                System.out.println("Fecha de nacimiento: " + autor.getAnoNacimiento());
                System.out.println("Fecha de fallecimiento: " + (autor.getAnoFallecimiento() != null ? autor.getAnoFallecimiento() : "Desconocido"));
                String libros = autor.getLibros().stream()
                        .map(Libro::getTitulo)
                        .collect(Collectors.joining(", "));
                System.out.println("Libros: [ " + libros + " ]");
            });
        }
    }

    private void listarAutoresVivosEnAno(Scanner scanner) {
        System.out.print("Ingrese el año para buscar autores vivos: ");
        int ano = scanner.nextInt();
        scanner.nextLine(); // Consumir el salto de línea

        List<Autor> autoresVivos = autorService.listarAutoresVivosEnAno(ano);
        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + ano);
        } else {
            autoresVivos.forEach(autor -> {
                System.out.println("-------AUTOR-------");
                System.out.println("Autor: " + autor.getNombre());
                System.out.println("Fecha de nacimiento: " + autor.getAnoNacimiento());
                System.out.println("Fecha de fallecimiento: " + (autor.getAnoFallecimiento() != null ? autor.getAnoFallecimiento() : "Desconocido"));
                System.out.println("Libros: " + autor.getLibros().size());
            });
        }
    }

    private void listarLibrosPorIdioma(Scanner scanner) {
        System.out.print("Ingrese el idioma (es, en, fr, pt): ");
        String idioma = scanner.nextLine();

        if (isIdiomaValido(idioma)) {
            libroService.listarLibrosPorIdioma(idioma).forEach(libro -> {
                System.out.println("------LIBRO--------");
                System.out.println("Título: " + libro.getTitulo());
                System.out.println("Autor: " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "Desconocido"));
                System.out.println("Idioma: " + libro.getIdioma());
                System.out.println("Número de descargas: " + libro.getNumeroDescargas());
            });
        } else {
            System.out.println("Idioma no válido. Intente de nuevo.");
        }
    }

    private boolean isIdiomaValido(String idioma) {
        return "es".equalsIgnoreCase(idioma) || "en".equalsIgnoreCase(idioma) || "fr".equalsIgnoreCase(idioma) || "pt".equalsIgnoreCase(idioma);
    }

    /**
     * Muestra los detalles de un libro DTO.
     *
     * @param libroDTO El objeto LibroDTO cuyos detalles se van a mostrar.
     */
    private void mostrarDetallesLibro(LibroDTO libroDTO) {
        System.out.println("------LIBRO--------");
        System.out.println("Título: " + libroDTO.getTitulo());
        System.out.println("Autor: " + (libroDTO.getAutores().isEmpty() ? "Desconocido" : libroDTO.getAutores().get(0).getNombre()));
        System.out.println("Idioma: " + libroDTO.getIdiomas().get(0));
        System.out.println("Número de descargas: " + libroDTO.getNumeroDescargas());
    }
}
