package carely.controller.pages;

import carely.error.AuthenticationException;
import carely.error.RepositoryException;
import carely.error.ValidationException;
import carely.model.User;
import carely.service.ProfileService;
import carely.utils.BackgroundTasks;
import carely.utils.ViewNavigator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;

public class ProfileController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final ProfileService profileService = new ProfileService();

    @FXML
    private Label profileAvatarLabel;

    @FXML
    private Label profileNameLabel;

    @FXML
    private Label profileRoleLabel;

    @FXML
    private Label profileEmailLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label memberSinceLabel;

    @FXML
    private Label updatedLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private DatePicker dateOfBirthPicker;

    @FXML
    private Label profileMessageLabel;

    @FXML
    private Button saveProfileButton;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label passwordMessageLabel;

    @FXML
    private Button changePasswordButton;

    @FXML
    private TextField deactivateConfirmationField;

    @FXML
    private Label actionMessageLabel;

    @FXML
    private Button deactivateButton;

    @FXML
    private void initialize() {
        genderComboBox.setItems(FXCollections.observableArrayList("Female", "Male", "Non-binary", "Prefer not to say"));
        loadProfile();
    }

    @FXML
    private void onSaveProfile() {
        clearMessage(profileMessageLabel);
        saveProfileButton.setDisable(true);
        BackgroundTasks.run(
                () -> profileService.updateProfile(
                        fullNameField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        genderComboBox.getValue(),
                        dateOfBirthPicker.getValue()
                ),
                user -> {
                    renderUser(user);
                    showSuccess(profileMessageLabel, "Profile updated.");
                },
                exception -> showProfileFailure(profileMessageLabel, exception, "Profile could not be saved. Check the database connection."),
                () -> saveProfileButton.setDisable(false)
        );
    }

    @FXML
    private void onChangePassword() {
        clearMessage(passwordMessageLabel);
        changePasswordButton.setDisable(true);
        BackgroundTasks.run(
                () -> {
                    profileService.changePassword(
                            currentPasswordField.getText(),
                            newPasswordField.getText(),
                            confirmPasswordField.getText()
                    );
                    return null;
                },
                ignored -> {
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                    showSuccess(passwordMessageLabel, "Password changed.");
                },
                exception -> showProfileFailure(passwordMessageLabel, exception, "Password could not be changed. Check the database connection."),
                () -> changePasswordButton.setDisable(false)
        );
    }

    @FXML
    private void onDeactivateAccount() {
        clearMessage(actionMessageLabel);
        deactivateButton.setDisable(true);
        BackgroundTasks.run(
                () -> {
                    profileService.deactivateCurrentAccount(deactivateConfirmationField.getText());
                    return null;
                },
                ignored -> ViewNavigator.showLogin(),
                exception -> showProfileFailure(actionMessageLabel, exception, "Account could not be deactivated. Check the database connection."),
                () -> deactivateButton.setDisable(false)
        );
    }

    private void loadProfile() {
        BackgroundTasks.run(
                profileService::loadCurrentUser,
                this::renderUser,
                exception -> showError(profileMessageLabel, "Profile could not be loaded."),
                () -> {
                }
        );
    }

    private void renderUser(User user) {
        profileAvatarLabel.setText(getInitials(user.getFullName()));
        profileNameLabel.setText(user.getFullName());
        profileRoleLabel.setText(user.getRole().getDisplayName());
        profileEmailLabel.setText(user.getEmail());
        statusLabel.setText(user.isActive() ? "Active" : "Inactive");
        memberSinceLabel.setText(user.getCreatedAt() == null ? "Not available" : DATE_FORMATTER.format(user.getCreatedAt()));
        updatedLabel.setText(user.getUpdatedAt() == null ? "Not available" : DATE_TIME_FORMATTER.format(user.getUpdatedAt()));

        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        genderComboBox.setValue(user.getGender());
        dateOfBirthPicker.setValue(user.getDateOfBirth());
    }

    private void clearMessage(Label label) {
        label.setText("");
        label.getStyleClass().removeAll("message-error", "message-success");
    }

    private void showSuccess(Label label, String message) {
        label.setText(message);
        label.getStyleClass().removeAll("message-error");
        if (!label.getStyleClass().contains("message-success")) {
            label.getStyleClass().add("message-success");
        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.getStyleClass().removeAll("message-success");
        if (!label.getStyleClass().contains("message-error")) {
            label.getStyleClass().add("message-error");
        }
    }

    private void showProfileFailure(Label label, Throwable exception, String repositoryMessage) {
        if (exception instanceof ValidationException || exception instanceof AuthenticationException) {
            showError(label, exception.getMessage());
        } else if (exception instanceof RepositoryException) {
            showError(label, repositoryMessage);
        } else {
            showError(label, "Something went wrong.");
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
