package com.proyecto.ui;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Muestra la imagen real de un producto si el archivo existe en
 * {@code assets/images/}; de lo contrario, pinta el ícono vectorial de la
 * categoría como fallback.
 *
 * <p>La imagen real se escala manteniendo su proporción ({@code preserveRatio})
 * para que no se vea distorsionada dentro del área dada. El ícono de
 * fallback se pinta del tamaño completo del área, igual que se veía antes.</p>
 *
 * <p>Las imágenes se suben desde el formulario de "Nuevo producto" y se
 * copian a {@code assets/images/} con su nombre original.</p>
 */
public class ImagenProducto extends StackPane {

    /**
     * @param nombreImagen nombre del archivo en {@code assets/images/} (puede ser null).
     * @param iconoSvg     ruta SVG del ícono de fallback (categoría).
     * @param ancho        ancho máximo del área.
     * @param alto         alto máximo del área.
     */
    public ImagenProducto(String nombreImagen, String iconoSvg, double ancho, double alto) {
        getStyleClass().add("imagen-producto");
        setPrefSize(ancho, alto);
        setMinSize(ancho, alto);
        setMaxSize(ancho, alto);
        setAlignment(Pos.CENTER);

        if (nombreImagen != null && !nombreImagen.isBlank()) {
            Path path = Paths.get("assets", "images", nombreImagen);
            if (Files.exists(path)) {
                Image imagen = new Image(path.toUri().toString());
                ImageView view = new ImageView(imagen);
                view.setPreserveRatio(true);
                view.setSmooth(true);
                // Escalar para que quepa completa dentro del área sin distorsionar.
                double scale = Math.min(ancho / imagen.getWidth(), alto / imagen.getHeight());
                view.setFitWidth(imagen.getWidth() * scale);
                view.setFitHeight(imagen.getHeight() * scale);
                getChildren().add(view);
                return;
            }
        }

        // Fallback: ícono vectorial de la categoría, tamaño completo del área.
        Icono icono = new Icono(iconoSvg, Math.min(ancho, alto) * 0.65);
        icono.getStyleClass().add("icono-imagen");
        getChildren().add(icono);
    }
}
