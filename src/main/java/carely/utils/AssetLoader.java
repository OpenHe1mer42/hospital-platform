package carely.utils;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AssetLoader {
    private static final String IMAGE_ROOT = "/images/";
    private static final Pattern SVG_PATH_PATTERN = Pattern.compile("<path[^>]*\\sd=\"([^\"]+)\"");

    private AssetLoader() {
    }

    public static ImageView imageView(String fileName, double size) {
        String resourcePath = resourcePath(fileName);
        var resource = AssetLoader.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Image resource not found: " + resourcePath);
        }

        Image image = new Image(resource.toExternalForm(), size, size, true, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("image-icon");
        return imageView;
    }

    public static Node icon(String fileName, double size) {
        if (fileName.toLowerCase().endsWith(".svg")) {
            return svgIcon(fileName, size);
        }
        return imageView(fileName, size);
    }

    private static StackPane svgIcon(String fileName, double size) {
        SVGPath path = new SVGPath();
        path.setContent(readSvgPath(fileName));
        path.getStyleClass().add("svg-icon");

        double bounds = Math.max(path.getLayoutBounds().getWidth(), path.getLayoutBounds().getHeight());
        if (bounds > 0) {
            double scale = size / bounds;
            path.setScaleX(scale);
            path.setScaleY(scale);
        }

        StackPane wrapper = new StackPane(path);
        wrapper.setMinSize(size, size);
        wrapper.setPrefSize(size, size);
        wrapper.setMaxSize(size, size);
        return wrapper;
    }

    private static String readSvgPath(String fileName) {
        String resourcePath = resourcePath(fileName);
        try (InputStream stream = AssetLoader.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("SVG resource not found: " + resourcePath);
            }

            String svg = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            Matcher matcher = SVG_PATH_PATTERN.matcher(svg);
            if (!matcher.find()) {
                throw new IllegalStateException("SVG path not found in resource: " + resourcePath);
            }
            return matcher.group(1);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read SVG resource: " + resourcePath, exception);
        }
    }

    private static String resourcePath(String fileName) {
        return IMAGE_ROOT + fileName;
    }
}
