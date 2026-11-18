# Tienda Virtual (JavaFX)

Tienda virtual de escritorio construida con JavaFX 17 y Maven, con
persistencia en JSON plano. Proyecto educativo con capas estrictas:
`ui (JavaFX) → servicio → persistencia → modelo`.

## Ejecutar

Requisitos: **Java 17** y **Maven 3.9**.

### Opción recomendada (sin advertencias)

```bash
mvn clean javafx:run
```

### Desde IntelliJ IDEA

- Ejecuta el goal de Maven `clean javafx:run` (por ejemplo, desde el panel
  Maven o con la consola integrada). Esto lanza la app con JavaFX en el
  module path, tal como está configurado en `pom.xml`.
- También puedes crear una configuración de ejecución de tipo **Maven** con
  los goals `clean javafx:run` y el `pom.xml` del proyecto.
- También puedes ejecutar la clase `com.proyecto.Principal` directamente: es
  un lanzador plano (`Application.launch(AppTienda.class, args)`) que
  funciona con JavaFX en el classpath. En ese modo JavaFX imprime una
  advertencia cosmética ("Unsupported JavaFX configuration: classes were
  loaded from 'unnamed module'"); la aplicación funciona igual.
- Ejecutar la clase `com.proyecto.AppTienda` directamente desde el IDE
  **no funciona**: la JVM exige los componentes de JavaFX en el module path
  y responde "faltan los componentes de JavaFX runtime".

### Desde NetBeans

1. Abre NetBeans y elige **File → Open Project**, selecciona la carpeta
   raíz del proyecto (donde está `pom.xml`).
2. NetBeans detectará el proyecto Maven automáticamente.
3. En la ventana **Projects**, haz clic derecho sobre el proyecto
   `universal-store` y selecciona **Custom → Goals...**
4. Escribe los goals:
   ```
   clean javafx:run
   ```
   y presiona **Run** (o **OK**).
5. También puedes abrir una terminal dentro de NetBeans (**Window → Tools →
   Terminal** o la terminal integrada) y ejecutar:
   ```bash
   mvn clean javafx:run
   ```
6. La aplicación usará la clase principal definida en `pom.xml`
   (`com.proyecto.AppTienda`) con JavaFX 17 en el module path.

> Nota: ejecutar la clase principal directamente desde el IDE **no
> funciona**, ya que la JVM requiere los componentes de JavaFX en el
> module path y devuelve el error "faltan los componentes de JavaFX
> runtime". Usa siempre el goal `javafx:run` de Maven.

También puedes ejecutar la clase `com.proyecto.Principal` directamente: es
un lanzador plano (`Application.launch(AppTienda.class, args)`) que
funciona con JavaFX en el classpath. En ese modo JavaFX imprime una
advertencia cosmética ("Unsupported JavaFX configuration: classes were
loaded from 'unnamed module'"); la aplicación funciona igual.

## Pruebas

```bash
mvn test
```

Los tests usan `data/test-*.json` y limpian sus archivos al terminar.

## Cuentas demo (datos sembrados al primer arranque)

| Usuario  | Clave     | Rol           |
|----------|-----------|---------------|
| admin    | admin123  | ADMINISTRADOR |
| cliente  | cli123    | CLIENTE       |
| vendedor | ven123    | VENDEDOR      |

## Estructura

```
src/main/java/com/proyecto/
├── modelo/        POJOs puros (Usuario, Producto, Categoria, ...)
├── persistencia/  Repositorios sobre JSON (Gson)
├── servicio/      Lógica de negocio (auth, productos, categorías)
├── util/          Hash de claves, IDs, paginación
├── estructuras/   PilaNavegacion (y otras estructuras didácticas)
└── ui/            Vistas JavaFX programáticas (sin FXML)
```

Los datos viven en `data/*.json` (excluidos del repositorio); las imágenes
de producto irán a `assets/images/`.
