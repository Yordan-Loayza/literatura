# Literalura

**Literalura** es una aplicación que permite interactuar con una base de datos de libros y autores, permitiendo a los usuarios realizar diferentes acciones como buscar libros, listar los libros y autores registrados, y obtener información sobre autores vivos en un año específico, entre otras funcionalidades.

## Funcionalidades

- **Buscar libro por título**: Permite al usuario buscar libros a través de una API externa, registrar nuevos libros si no están en la base de datos, o mostrar detalles de los libros encontrados.
- **Listar libros registrados**: Muestra todos los libros registrados en la base de datos.
- **Listar autores registrados**: Muestra todos los autores registrados en la base de datos, junto con sus libros.
- **Listar autores vivos en un año**: Permite al usuario ingresar un año y listar los autores que estuvieron vivos en ese año.
- **Listar libros por idioma**: Permite filtrar y mostrar libros registrados según el idioma seleccionado.

---
### Pasos
1. Clona el repositorio:
   ```bash
   git clone https://github.com/Yordan-Loayza/literatura.git
   cd literatura
   ```
2. Configura tu base de datos postgresql en application.properties:
   ```bash
   spring.datasource.url=jdbc:postgresql://localhost:5432/literalura
   spring.datasource.username=tu_usuario
   spring.datasource.password=tu_contraseña
   ```
3. A correr el programa:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
