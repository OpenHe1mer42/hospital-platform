package carely.controller.auth;

import carely.error.AuthenticationException;
import carely.error.RepositoryException;
import carely.error.ValidationException;
import carely.model.User;
import carely.service.AuthService;
import carely.service.AuthSession;
import carely.utils.AssetLoader;
import carely.utils.BackgroundTasks;
import carely.utils.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class LoginController {
    private final AuthService authService = new AuthService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    @FXML
    private StackPane loginLogoSlot;

    @FXML
    private void initialize() {
        loginLogoSlot.getChildren().setAll(AssetLoader.imageView("logo.png", 96));
    }

    @FXML
    private void onLogin() {
        clearMessage();
        loginButton.setDisable(true);
        BackgroundTasks.run(
                () -> authService.login(emailField.getText(), passwordField.getText()),
                this::openMainLayout,
                this::showLoginFailure,
                () -> loginButton.setDisable(false)
        );
    }

    @FXML
    private void onShowSignup() {
        ViewNavigator.showSignup();
    }

    private void clearMessage() {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("message-error", "message-success");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("message-success");
        if (!messageLabel.getStyleClass().contains("message-error")) {
            messageLabel.getStyleClass().add("message-error");
        }
    }

    private void openMainLayout(User user) {
        try {
            AuthSession.start(user);
            ViewNavigator.showMainLayout();
        } catch (RuntimeException exception) {
            showError("Login worked, but the main page could not be opened.");
            ViewNavigator.showErrorDialog("Navigation failed", "Login worked, but the main page could not be opened: " + rootCauseMessage(exception));
        }
    }

    private void showLoginFailure(Throwable exception) {
        if (exception instanceof ValidationException || exception instanceof AuthenticationException) {
            showError(exception.getMessage());
            ViewNavigator.showErrorDialog("Login failed", exception.getMessage());
        } else if (exception instanceof RepositoryException) {
            showError("Database connection failed. Check your Carely database settings.");
            ViewNavigator.showErrorDialog("Login failed", "Database connection failed. Check your Carely database settings.");
        } else {
            showError("Login failed.");
            ViewNavigator.showErrorDialog("Login failed", rootCauseMessage(exception));
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
