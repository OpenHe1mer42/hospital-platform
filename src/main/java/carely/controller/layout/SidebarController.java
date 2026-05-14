package carely.controller.layout;

import carely.utils.AssetLoader;
import carely.utils.PageRoute;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

public class SidebarController {
    private final Map<PageRoute, Button> navButtons = new EnumMap<>(PageRoute.class);
    private MainLayoutController mainLayoutController;

    @FXML
    private VBox navContainer;

    @FXML
    private StackPane brandIconSlot;

    @FXML
    private void initialize() {
        brandIconSlot.getChildren().setAll(AssetLoader.imageView("logo.png", 30));

        for (PageRoute route : PageRoute.values()) {
            if (!route.isSidebarVisible()) {
                continue;
            }
            Button button = createNavButton(route);
            navButtons.put(route, button);
            navContainer.getChildren().add(button);
        }
    }

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public void setActiveRoute(PageRoute activeRoute) {
        navButtons.forEach((route, button) -> {
            if (route == activeRoute) {
                if (!button.getStyleClass().contains("active")) {
                    button.getStyleClass().add("active");
                }
            } else {
                button.getStyleClass().remove("active");
            }
        });
    }

    private Button createNavButton(PageRoute route) {
        Node icon = AssetLoader.icon(route.getIconFileName(), 19);
        icon.getStyleClass().add("nav-icon");

        Label title = new Label(route.getTitle());
        title.getStyleClass().add("nav-label");

        HBox content = new HBox(14, icon, title);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.ALWAYS);

        Button button = new Button();
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("sidebar-item");
        button.setMnemonicParsing(false);
        button.setOnAction(event -> {
            if (mainLayoutController != null) {
                mainLayoutController.navigateTo(route);
            }
        });
        return button;
    }
}
