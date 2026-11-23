# Universal Store (JavaFX)

Tienda virtual de escritorio construida con **JavaFX 17** y **Maven**, con
persistencia en JSON plano. Proyecto educativo con capas estrictas:
`ui (JavaFX) → servicio → persistencia → modelo`.

---

## Tabla de contenidos

1. [Requisitos](#requisitos)
2. [Ejecutar la aplicación](#ejecutar-la-aplicación)
3. [Cuentas de demostración](#cuentas-de-demostración)
4. [Funcionalidades principales](#funcionalidades-principales)
    - [Rol: Cliente](#rol-cliente)
    - [Rol: Vendedor](#rol-vendedor)
    - [Rol: Administrador](#rol-administrador)
5. [Flujo de venta completo](#flujo-de-venta-completo)
6. [Pruebas](#pruebas)
7. [Estructura del proyecto](#estructura-del-proyecto)
8. [Datos e imágenes](#datos-e-imágenes)

---

## Requisitos

- **Java 17**
- **Maven 3.9+**

---

## Ejecutar la aplicación

### Opción recomendada (línea de comandos)

```bash
mvn clean javafx:run
```

Esto lanza la aplicación con JavaFX en el *module path*, tal como está
configurado en `pom.xml`. Es la forma más limpia y sin advertencias.

### Desde IntelliJ IDEA

1. Abre el proyecto en IntelliJ (es un proyecto Maven).
2. En el panel **Maven**, busca `universal-store` → `Plugins` → `javafx`
   y ejecuta el goal `javafx:run`.
    - También puedes crear una configuración de ejecución de tipo **Maven**
      con los goals `clean javafx:run` y el `pom.xml` del proyecto.
3. También puedes ejecutar la clase `com.proyecto.Principal` directamente:
   es un lanzador plano
   (`Application.launch(AppTienda.class, args)`) que funciona con JavaFX
   en el classpath. En ese modo JavaFX imprime una advertencia cosmética
   ("Unsupported JavaFX configuration: classes were loaded from
   'unnamed module'"); la aplicación funciona igual.
4. Ejecutar la clase `com.proyecto.AppTienda` directamente desde el IDE
   **no funciona**: la JVM exige los componentes de JavaFX en el module
   path y responde "faltan los componentes de JavaFX runtime".

### Desde NetBeans

1. Abre NetBeans y elige **File → Open Project**, selecciona la carpeta
   raíz del proyecto (donde está `pom.xml`).
2. NetBeans detectará el proyecto Maven automáticamente.
3. En la ventana **Projects**, haz clic derecho sobre el proyecto y
   selecciona **Custom → Goals...**
4. Escribe los goals:
   ```
   clean javafx:run
   ```
   y presiona **Run**.
5. O bien, abre una terminal dentro de NetBeans y ejecuta:
   ```bash
   mvn clean javafx:run
   ```
6. Al igual que con Intellij, puedes ejecutar la clase `com.proyecto.Principal` directamente:
   es un lanzador plano
   (`Application.launch(AppTienda.class, args)`) que funciona con JavaFX
   en el classpath. En ese modo JavaFX imprime una advertencia cosmética
   ("Unsupported JavaFX configuration: classes were loaded from
   'unnamed module'"); la aplicación funciona igual.

---

## Cuentas de demostración

Al primer arranque, si la carpeta `data/` está vacía, la aplicación siembra
usuarios, categorías, productos, reseñas y una lista de deseos demo:

| Usuario  | Clave    | Rol           |
|----------|----------|---------------|
| admin    | admin123 | ADMINISTRADOR |
| cliente  | cli123   | CLIENTE       |
| vendedor | ven123   | VENDEDOR      |

---

## Funcionalidades principales

### Rol: Cliente

El cliente puede comprar, gestionar su carrito, deseos, pedidos y reseñas.

| Módulo                  | Qué permite hacer                                                                                                                                                                                                    |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Catálogo**            | Buscar productos por título/descripción, filtrar por categoría y rango de precio, ordenar (relevancia, nombre, precio), paginar (8/12/20 por página).                                                                |
| **Detalle de producto** | Ver ficha completa (imagen, precio, stock, descripción, calificación promedio, reseñas). Navegar atrás/adelante con la pila de rutas.                                                                                |
| **Carrito**             | Agregar productos, cambiar cantidades, quitar ítems, vaciar carrito. Totales con impuesto del 19%. Historial de deshacer/rehacer (Ctrl+Z / Ctrl+Y).                                                                  |
| **Deseos**              | Guardar productos para después sin comprometer stock. Moverlos al carrito o quitarlos. Persiste con la cuenta.                                                                                                       |
| **Mis pedidos**         | Ver historial de compras con estado (PENDIENTE → ENVIADO → ENTREGADO → CANCELADO). Cancelar pedidos pendientes (restaura stock). **Marcar como recibido** cuando el pedido está ENVIADO, cerrando el flujo de venta. |
| **Reseñas**             | Dejar una reseña (1–5 estrellas + comentario) por producto. Actualizar la propia reseña si ya existe.                                                                                                                |

---

### Rol: Vendedor

El vendedor publica productos, gestiona su stock y despacha pedidos.

| Módulo                      | Qué permite hacer                                                                                                                                                                                                                    |
|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Cola de despacho (FIFO)** | Ver pedidos pendientes que contienen sus productos, ordenados de más antiguo a más reciente. "Despachar siguiente" pasa el primero a ENVIADO. "Marcar entregado" pasa un pedido ENVIADO a ENTREGADO.                                 |
| **Mi stock**                | Ver productos publicados con stock actual. Editar el stock de cada producto. Chips de estado: OK, Stock bajo (< 5), AGOTADO.                                                                                                         |
| **Nuevo producto**          | Publicar un producto: título, descripción, precio, stock, categoría aprobada y imagen opcional (se copia a `assets/images/`). Si ninguna categoría encaja, solicitar una nueva al administrador con justificación (queda PENDIENTE). |

---

### Rol: Administrador

El admin gobierna el sistema: usuarios, catálogo global, categorías y métricas.

| Módulo              | Qué permite hacer                                                                                                                                                                                                           |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Usuarios**        | Listar todas las cuentas con rol y estado. Bloquear/desbloquear usuarios (un bloqueado no puede iniciar sesión). Cambiar rol (CLIENTE ↔ VENDEDOR ↔ ADMINISTRADOR). El admin no puede modificar su propia cuenta desde aquí. |
| **Catálogo global** | Ver todos los productos de todos los vendedores. Eliminar cualquier producto.                                                                                                                                               |
| **Categorías**      | Bandeja de solicitudes de categoría enviadas por vendedores. **Aprobar** (crea la categoría y queda disponible) o **rechazar** (no crea nada).                                                                              |
| **Métricas**        | Tablero de salud del negocio: ingresos totales (excluye cancelados), pedidos por estado, ventas por vendedor, producto más vendido.                                                                                         |

---

## Flujo de venta completo

1. El **cliente** navega el catálogo y agrega productos al carrito.
2. Finaliza la compra → se crea un pedido **PENDIENTE** y se descuenta el stock (compra todo-o-nada: se valida que haya
   stock de todo antes de tocar nada).
3. El **vendedor** ve el pedido en su cola FIFO y lo despacha → pasa a **ENVIADO**.
4. El **cliente** ve el pedido en "Mis pedidos" y hace clic en **"Marcar como recibido"** → pasa a **ENTREGADO**. El
   flujo de venta queda cerrado.
5. Si el pedido aún está **PENDIENTE**, el cliente puede **cancelarlo** y se restaura el stock.

---

## Pruebas

```bash
mvn test
```

Los tests usan archivos temporales en `data/test-*.json` y los borran al
terminar (`@AfterEach`).

---

## Estructura del proyecto

```
src/main/java/com/proyecto/
├── modelo/        POJOs puros (Usuario, Producto, Categoria, Pedido, Deseos, ...)
├── persistencia/  Repositorios sobre JSON (Gson): Usuarios, Productos, Categorías,
│                  SolicitudesCategoria, Pedidos, Deseos
├── servicio/      Lógica de negocio: Auth, Productos, Categorías, Carrito, Pedidos,
│                  Reseñas, Deseos, Usuarios, Métricas
├── util/          Hash de claves (SHA-256), Generador de IDs, Paginador
├── estructuras/   PilaNavegacion (LIFO atrás/adelante), Historial (LIFO deshacer/rehacer),
│                  ColaFIFO (FIFO despacho)
└── ui/            Vistas JavaFX programáticas (sin FXML): Login, Registro, Catálogo,
                   Detalle, Carrito, Deseos, Pedidos, y módulos de Vendedor y Admin
```

Capas estrictas: **`ui → servicio → persistencia → modelo`**.

- La UI no lee/escribe ficheros ni llama a Gson.
- El servicio no importa JavaFX.

---

## Datos e imágenes

- `data/*.json` — archivos de runtime (usuarios, productos, categorías, solicitudes, pedidos, deseos). Están en
  `.gitignore`: no se commitean.
- `assets/images/` — imágenes de producto subidas desde el formulario de "Nuevo producto". Si no hay imagen, se muestra
  un ícono vectorial de la categoría como fallback.

---

## Tecnologías

- Java 17, JavaFX 17, Maven, Gson
- CSS con looked-up colors (tema claro/oscuro intercambiable)
- Iconos vectoriales SVG vía `-fx-shape`
