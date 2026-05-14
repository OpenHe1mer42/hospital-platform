package carely.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public final class ViewNavigator {
    private static final String STYLESHEET = "/assets/css/app.css";
    private static Stage stage;
    private static Scene authScene;
    private static Scene mainScene;

    private ViewNavigator() {
    }

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void showLogin() {
        show("/views/auth/login.fxml", SceneGroup.AUTH);
    }

    public static void showSignup() {
        show("/views/auth/signup.fxml", SceneGroup.AUTH);
    }

    public static void showMainLayout() {
        show("/views/layout/main-layout.fxml", SceneGroup.MAIN);
    }

    public static Parent loadPage(PageRoute route) {
        if (route.getFxmlPath() != null) {
            return load(route.getFxmlPath());
        }
        return createPlaceholderPage(route);
    }

    public static void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void show(String fxmlPath, SceneGroup group) {
        ensureStage();
        Parent root = load(fxmlPath);
        Scene scene = sceneFor(group, root);
        scene.setRoot(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private static Scene sceneFor(SceneGroup group, Parent root) {
        return switch (group) {
            case AUTH -> {
                if (authScene == null) {
                    authScene = createScene(root);
                }
                yield authScene;
            }
            case MAIN -> {
                if (mainScene == null) {
                    mainScene = createScene(root);
                }
                yield mainScene;
            }
        };
    }

    private static Scene createScene(Parent root) {
        var bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        scene.getStylesheets().add(resourceUrl(STYLESHEET));
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        return scene;
    }

    private static Parent load(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(ViewNavigator.class.getResource(fxmlPath));
            return loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load view: " + fxmlPath, exception);
        }
    }

    private static Parent createPlaceholderPage(PageRoute route) {
        VBox page = new VBox();
        page.getStyleClass().add("page-placeholder");
        Label title = new Label(route.getTitle());
        title.getStyleClass().add("page-title");
        Region filler = new Region();
        VBox.setVgrow(filler, Priority.ALWAYS);
        page.getChildren().addAll(title, filler);
        return page;
    }

    private static String resourceUrl(String resourcePath) {
        var resource = ViewNavigator.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }
        return resource.toExternalForm();
    }

    private static void ensureStage() {
        if (stage == null) {
            throw new IllegalStateException("Primary stage has not been registered.");
        }
    }

    private enum SceneGroup {
        AUTH,
        MAIN
    }
}
