package com.proyecto.ui;

import com.proyecto.modelo.Categoria;
import com.proyecto.modelo.Usuario;
import com.proyecto.servicio.ExcepcionNegocio;
import com.proyecto.servicio.ServicioProductos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Formulario de publicación de productos para el vendedor.
 *
 * <p>El combo solo ofrece categorías existentes (aprobadas). Si ninguna
 * encaja, el vendedor puede escribir una categoría nueva con justificación:
 * queda como solicitud PENDIENTE para que un administrador la apruebe o
 * rechace.</p>
 */
public class VistaNuevoProducto {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;
    private final Runnable alVolverAlCatalogo;

    private final VBox raiz = new VBox(14);
    private final TextField campoTitulo = new TextField();
    private final TextField campoDescripcion = new TextField();
    private final TextField campoPrecio = new TextField();
    private final TextField campoStock = new TextField();
    private final TextField campoNombreImagen = new TextField();
    private final Button btnSeleccionarImagen = new Button("Seleccionar imagen…");
    private String nombreImagenSeleccionada = null;
    private final ComboBox<Categoria> comboCategoria = new ComboBox<>();
    private final CheckBox chkCategoriaNueva = new CheckBox("Solicitar una categoría nueva");
    private final TextField campoNuevaCategoria = new TextField();
    private final TextArea campoJustificacion = new TextArea();
    private final Button btnPublicar = new Button("Publicar producto");

    public VistaNuevoProducto(GestorEscenas app,
                              BiConsumer<Avisos.Tipo, String> avisador,
                              Runnable alVolverAlCatalogo) {
        this.app = app;
        this.avisador = avisador;
        this.alVolverAlCatalogo = alVolverAlCatalogo;
        construir();
        cargarCategorias();
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        Label titulo = new Label("Nuevo producto");
        titulo.getStyleClass().add("titulo-panel");
        Label subtitulo = new Label("Publica un artículo en el catálogo para que los clientes lo compren");
        subtitulo.getStyleClass().add("texto-apagado");

        VBox formulario = new VBox(10);
        formulario.getStyleClass().add("panel");
        formulario.setPadding(new Insets(20));

        campoTitulo.getStyleClass().add("campo");
        campoTitulo.setPromptText("Título del producto");

        campoDescripcion.getStyleClass().add("campo");
        campoDescripcion.setPromptText("Descripción");

        campoPrecio.getStyleClass().add("campo");
        campoPrecio.setPromptText("Precio");

        campoStock.getStyleClass().add("campo");
        campoStock.setPromptText("Stock");

        comboCategoria.getStyleClass().add("campo");
        comboCategoria.setPromptText("Categoría");
        comboCategoria.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categoria categoria) {
                return categoria == null ? "" : categoria.getNombre();
            }

            @Override
            public Categoria fromString(String string) {
                return null;
            }
        });

        // --- Imagen del producto ---
        HBox filaImagen = new HBox(10);
        filaImagen.setAlignment(Pos.CENTER_LEFT);
        Label lblImagen = new Label("Imagen:");
        lblImagen.getStyleClass().add("etiqueta");
        campoNombreImagen.getStyleClass().add("campo");
        campoNombreImagen.setPromptText("Sin imagen");
        campoNombreImagen.setDisable(true);
        campoNombreImagen.setPrefWidth(220);
        btnSeleccionarImagen.getStyleClass().add("btn");
        btnSeleccionarImagen.setOnAction(e -> seleccionarImagen());
        filaImagen.getChildren().addAll(lblImagen, campoNombreImagen, btnSeleccionarImagen);

        Label lblCategoriaNueva = new Label("O solicitar una categoría nueva:");
        lblCategoriaNueva.getStyleClass().add("etiqueta");

        campoNuevaCategoria.getStyleClass().add("campo");
        campoNuevaCategoria.setPromptText("Nombre de la categoría");
        campoNuevaCategoria.setDisable(true);

        campoJustificacion.getStyleClass().add("campo-texto");
        campoJustificacion.setPromptText("¿Por qué necesitas esta categoría?");
        campoJustificacion.setPrefRowCount(3);
        campoJustificacion.setDisable(true);

        chkCategoriaNueva.setOnAction(e -> {
            boolean activa = chkCategoriaNueva.isSelected();
            campoNuevaCategoria.setDisable(!activa);
            campoJustificacion.setDisable(!activa);
            comboCategoria.setDisable(activa);
        });

        btnPublicar.getStyleClass().addAll("btn", "btn-primario");
        btnPublicar.setMaxWidth(Double.MAX_VALUE);
        btnPublicar.setOnAction(e -> publicar());

        Button btnVolver = new Button("Volver al catálogo");
        btnVolver.getStyleClass().add("btn");
        btnVolver.setMaxWidth(Double.MAX_VALUE);
        btnVolver.setOnAction(e -> alVolverAlCatalogo.run());

        formulario.getChildren().addAll(
                campoTitulo, campoDescripcion,
                campoPrecio, campoStock, comboCategoria, filaImagen,
                chkCategoriaNueva, lblCategoriaNueva, campoNuevaCategoria, campoJustificacion,
                btnPublicar, btnVolver);

        raiz.getChildren().addAll(titulo, subtitulo, formulario);
    }

    private void cargarCategorias() {
        List<Categoria> categorias = app.getServicioCategorias().listarParaPublicar();
        comboCategoria.getItems().setAll(categorias);
        if (!categorias.isEmpty()) {
            comboCategoria.setValue(categorias.get(0));
        }
    }

    private void seleccionarImagen() {
        Stage escenario = app.getEscenario();
        FileChooser seleccionador = new FileChooser();
        seleccionador.setTitle("Seleccionar imagen del producto");
        seleccionador.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        File archivo = seleccionador.showOpenDialog(escenario);
        if (archivo != null) {
            try {
                Path destinoDir = Paths.get("assets", "images");
                Files.createDirectories(destinoDir);
                Path destino = destinoDir.resolve(archivo.getName());
                Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                nombreImagenSeleccionada = archivo.getName();
                campoNombreImagen.setText(nombreImagenSeleccionada);
                avisador.accept(Avisos.Tipo.EXITO, "Imagen seleccionada: " + nombreImagenSeleccionada);
            } catch (IOException e) {
                avisador.accept(Avisos.Tipo.ERROR, "No se pudo guardar la imagen: " + e.getMessage());
            }
        }
    }

    private void publicar() {
        try {
            String titulo = campoTitulo.getText();
            String descripcion = campoDescripcion.getText();
            double precio = Double.parseDouble(campoPrecio.getText().trim().replace(',', '.'));
            int stock = Integer.parseInt(campoStock.getText().trim());
            String categoriaId;

            Usuario vendedor = app.getSesion().getUsuario();

            if (chkCategoriaNueva.isSelected()) {
                // Solicitar categoría nueva (queda PENDIENTE para el admin)
                String nombre = campoNuevaCategoria.getText();
                String justificacion = campoJustificacion.getText();
                app.getServicioCategorias().solicitar(nombre, justificacion, vendedor);
                avisador.accept(Avisos.Tipo.EXITO,
                        "Solicitud de categoría enviada. Un administrador la revisará pronto.");
                limpiarFormulario();
                return;
            }

            Categoria seleccionada = comboCategoria.getValue();
            if (seleccionada == null) {
                throw new ExcepcionNegocio("Selecciona una categoría");
            }
            categoriaId = seleccionada.getId();

            ServicioProductos servicio = app.getServicioProductos();
            servicio.crear(new ServicioProductos.DatosProducto(
                    titulo, descripcion, precio, stock, categoriaId, nombreImagenSeleccionada), vendedor.getId());

            avisador.accept(Avisos.Tipo.EXITO, "Producto publicado en el catálogo");
            limpiarFormulario();
        } catch (NumberFormatException e) {
            avisador.accept(Avisos.Tipo.ERROR, "Precio y stock deben ser números válidos");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private void limpiarFormulario() {
        campoTitulo.clear();
        campoDescripcion.clear();
        campoPrecio.clear();
        campoStock.clear();
        campoNombreImagen.clear();
        nombreImagenSeleccionada = null;
        chkCategoriaNueva.setSelected(false);
        campoNuevaCategoria.setDisable(true);
        campoJustificacion.setDisable(true);
        comboCategoria.setDisable(false);
        cargarCategorias();
    }

    private static String formatear(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
