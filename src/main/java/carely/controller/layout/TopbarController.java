package carely.controller.layout;

import carely.model.User;
import carely.service.AuthSession;
import carely.utils.AssetLoader;
import carely.utils.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class TopbarController {
    @FXML
    private Button menuButton;

    @FXML
    private Label currentPageLabel;

    @FXML
    private StackPane searchIconSlot;

    @FXML
    private StackPane notificationIconSlot;

    @FXML
    private StackPane helpIconSlot;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private Label avatarLabel;

    private Runnable sidebarToggleHandler;
    private Runnable profileClickHandler;
    private final Runnable sessionChangeListener = this::refreshUser;

    @FXML
    private void initialize() {
        searchIconSlot.getChildren().setAll(AssetLoader.icon("search.svg", 18));
        notificationIconSlot.getChildren().setAll(AssetLoader.icon("notification.svg", 20));
        helpIconSlot.getChildren().setAll(AssetLoader.icon("help.svg", 20));
        AuthSession.addChangeListener(sessionChangeListener);
    }

    public void setCurrentPage(String title) {
        if (currentPageLabel != null) {
            currentPageLabel.setText(title);
        }
    }

    public void setSidebarToggleHandler(Runnable sidebarToggleHandler) {
        this.sidebarToggleHandler = sidebarToggleHandler;
    }

    public void setProfileClickHandler(Runnable profileClickHandler) {
        this.profileClickHandler = profileClickHandler;
    }

    public void setSidebarCollapsed(boolean collapsed) {
        menuButton.setText(collapsed ? "☰" : "☰");
        menuButton.setAccessibleText(collapsed ? "Open sidebar" : "Close sidebar");
    }

    public void refreshUser() {
        User user = AuthSession.getCurrentUser().orElse(null);
        if (user == null) {
            userNameLabel.setText("Carely User");
            userRoleLabel.setText("Signed out");
            avatarLabel.setText("CU");
            return;
        }

        userNameLabel.setText(user.getFullName());
        userRoleLabel.setText(user.getRole().getDisplayName());
        avatarLabel.setText(getInitials(user.getFullName()));
    }

    @FXML
    private void onLogout(ActionEvent event) {
        event.consume();
        AuthSession.clear();
        ViewNavigator.showLogin();
    }

    @FXML
    private void onToggleSidebar() {
        if (sidebarToggleHandler != null) {
            sidebarToggleHandler.run();
        }
    }

    @FXML
    private void onOpenProfile(MouseEvent event) {
        event.consume();
        if (profileClickHandler != null) {
            profileClickHandler.run();
        }
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "CU";
        }

        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + second).toUpperCase();
    }
}
