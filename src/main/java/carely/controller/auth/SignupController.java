package carely.controller.auth;

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

public class SignupController {
    private final AuthService authService = new AuthService();

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private CheckBox termsCheckBox;

    @FXML
    private Button signupButton;

    @FXML
    private Label messageLabel;

    @FXML
    private StackPane signupLogoSlot;

    @FXML
    private void initialize() {
        signupLogoSlot.getChildren().setAll(AssetLoader.imageView("logo.png", 96));
    }

    @FXML
    private void onSignup() {
        clearMessage();
        if (!termsCheckBox.isSelected()) {
            showError("Please accept the terms before creating an account.");
            return;
        }

        signupButton.setDisable(true);
        BackgroundTasks.run(
                () -> authService.signup(
                    fullNameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    confirmPasswordField.getText()
                ),
                this::openMainLayout,
                this::showSignupFailure,
                () -> signupButton.setDisable(false)
        );
    }

    @FXML
    private void onShowLogin() {
        ViewNavigator.showLogin();
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
            showError("Account created, but the main page could not be opened.");
            ViewNavigator.showErrorDialog("Navigation failed", "Account created, but the main page could not be opened: " + rootCauseMessage(exception));
        }
    }

    private void showSignupFailure(Throwable exception) {
        if (exception instanceof ValidationException) {
            showError(exception.getMessage());
            ViewNavigator.showErrorDialog("Signup failed", exception.getMessage());
        } else if (exception instanceof RepositoryException) {
            showError("Database connection failed. Check your Carely database settings.");
            ViewNavigator.showErrorDialog("Signup failed", "Database connection failed. Check your Carely database settings.");
        } else {
            showError("Signup failed.");
            ViewNavigator.showErrorDialog("Signup failed", rootCauseMessage(exception));
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
