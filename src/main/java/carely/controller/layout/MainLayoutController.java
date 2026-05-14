package carely.controller.layout;

import carely.utils.PageRoute;
import carely.utils.ViewNavigator;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;

public class MainLayoutController {
    private static final double SIDEBAR_WIDTH = 250;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 0;
    private static final Duration SIDEBAR_ANIMATION_DURATION = Duration.millis(240);

    @FXML
    private BorderPane root;

    @FXML
    private StackPane contentPane;

    @FXML
    private BorderPane mainRegion;

    private SidebarController sidebarController;

    private TopbarController topbarController;

    private Region sidebarNode;

    private Rectangle sidebarClip;

    private Timeline sidebarAnimation;

    private boolean sidebarCollapsed;

    @FXML
    private void initialize() {
        loadShellControllers();
        sidebarController.setMainLayoutController(this);
        topbarController.setSidebarToggleHandler(this::toggleSidebar);
        topbarController.setProfileClickHandler(() -> navigateTo(PageRoute.PROFILE));
        topbarController.refreshUser();
        navigateTo(PageRoute.DASHBOARD);
    }

    public void navigateTo(PageRoute route) {
        Parent page = ViewNavigator.loadPage(route);
        contentPane.getChildren().setAll(page);
        sidebarController.setActiveRoute(route);
        topbarController.setCurrentPage(route.getTitle());
    }

    private void loadShellControllers() {
        LoadedFxml<SidebarController> sidebar = loadFxml("/views/layout/sidebar.fxml");
        LoadedFxml<TopbarController> topbar = loadFxml("/views/layout/topbar.fxml");
        sidebarController = sidebar.controller();
        topbarController = topbar.controller();
        sidebarNode = asRegion(sidebar.node());
        configureSidebarAnimation();
        root.setLeft(sidebarNode);
        mainRegion.setTop(topbar.node());
    }

    private void toggleSidebar() {
        setSidebarCollapsed(!sidebarCollapsed);
    }

    private void setSidebarCollapsed(boolean collapsed) {
        sidebarCollapsed = collapsed;

        if (sidebarAnimation != null) {
            sidebarAnimation.stop();
        }

        double targetWidth = collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH;
        double targetOpacity = collapsed ? 0 : 1;

        sidebarNode.setMouseTransparent(collapsed);
        sidebarNode.setVisible(true);

        sidebarAnimation = new Timeline(new KeyFrame(
                SIDEBAR_ANIMATION_DURATION,
                new KeyValue(sidebarNode.minWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
                new KeyValue(sidebarNode.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
                new KeyValue(sidebarNode.maxWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
                new KeyValue(sidebarNode.opacityProperty(), targetOpacity, Interpolator.EASE_BOTH),
                new KeyValue(sidebarClip.widthProperty(), targetWidth, Interpolator.EASE_BOTH)
        ));
        sidebarAnimation.setOnFinished(event -> {
            sidebarNode.setMouseTransparent(collapsed);
            topbarController.setSidebarCollapsed(collapsed);
        });
        sidebarAnimation.play();
    }

    private void configureSidebarAnimation() {
        sidebarClip = new Rectangle(SIDEBAR_WIDTH, 0);
        sidebarClip.heightProperty().bind(root.heightProperty());
        sidebarNode.setClip(sidebarClip);
        sidebarNode.setMinWidth(SIDEBAR_WIDTH);
        sidebarNode.setPrefWidth(SIDEBAR_WIDTH);
        sidebarNode.setMaxWidth(SIDEBAR_WIDTH);
    }

    private Region asRegion(Node node) {
        if (node instanceof Region region) {
            return region;
        }
        throw new IllegalStateException("Sidebar root must be a Region to animate its width.");
    }

    private <T> LoadedFxml<T> loadFxml(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainLayoutController.class.getResource(fxmlPath));
            Node node = loader.load();
            T controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Missing controller for " + fxmlPath);
            }
            return new LoadedFxml<>(node, controller);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load layout part: " + fxmlPath, exception);
        }
    }

    private record LoadedFxml<T>(Node node, T controller) {
    }
}
