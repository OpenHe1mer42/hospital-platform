package carely.controller.pages;

import carely.error.RepositoryException;
import carely.error.ValidationException;
import carely.model.User;
import carely.model.UserRole;
import carely.service.AuthService;
import carely.service.AuthSession;
import carely.utils.AssetLoader;
import carely.utils.BackgroundTasks;
import carely.utils.ViewNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class DoctorsController {
    private static final String ALL_STATUSES = "All statuses";
    private static final String ACTIVE_STATUS = "Active";
    private static final String INACTIVE_STATUS = "Inactive";
    private static final double TABLE_HEADER_HEIGHT = 38;
    private static final double TABLE_ROW_HEIGHT = 52;
    private static final double EMPTY_TABLE_HEIGHT = 120;

    private final AuthService authService = new AuthService();
    private final ObservableList<User> doctors = FXCollections.observableArrayList();

    @FXML
    private TextField searchFilterField;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private Button openCreateDoctorButton;

    @FXML
    private Label listMessageLabel;

    @FXML
    private TableView<User> doctorsTable;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private VBox createDoctorDialog;

    @FXML
    private Label doctorDialogTitleLabel;

    @FXML
    private Label doctorDialogSubtitleLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private Label createMessageLabel;

    @FXML
    private Button createDoctorButton;

    private User editingDoctor;

    @FXML
    private void initialize() {
        configureTable();
        configureFilters();
        activeCheckBox.setSelected(true);
        hideCreateDoctorModal();

        if (!AuthSession.hasRole(UserRole.ADMIN)) {
            showError(listMessageLabel, "You do not have permission to manage doctor accounts.");
            openCreateDoctorButton.setDisable(true);
            doctorsTable.setDisable(true);
            return;
        }

        loadDoctors(null);
    }

    @FXML
    private void onFilterDoctors() {
        loadDoctors(null);
    }

    @FXML
    private void onClearFilters() {
        searchFilterField.clear();
        statusFilterComboBox.setValue(ALL_STATUSES);
        loadDoctors(null);
    }

    @FXML
    private void onOpenCreateDoctorModal() {
        openCreateDialog();
    }

    @FXML
    private void onCloseCreateDoctorModal() {
        hideCreateDoctorModal();
    }

    @FXML
    private void onCreateDoctor() {
        clearMessage(createMessageLabel);
        createDoctorButton.setDisable(true);
        BackgroundTasks.run(
                this::saveDoctor,
                this::handleDoctorSaved,
                this::showCreateFailure,
                () -> createDoctorButton.setDisable(!AuthSession.hasRole(UserRole.ADMIN))
        );
    }

    private void configureTable() {
        doctorsTable.setItems(doctors);
        doctorsTable.setPlaceholder(new Label("No doctors found."));
        doctorsTable.setFixedCellSize(TABLE_ROW_HEIGHT);
        doctorsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        doctors.addListener((ListChangeListener<User>) change -> updateTableHeight());
        fullNameColumn.setCellValueFactory(data -> new SimpleStringProperty(safeText(data.getValue().getFullName())));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(safeText(data.getValue().getEmail())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? ACTIVE_STATUS : INACTIVE_STATUS));
        actionsColumn.setCellFactory(column -> new ActionCell());
        updateTableHeight();
    }

    private void configureFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(ALL_STATUSES, ACTIVE_STATUS, INACTIVE_STATUS));
        statusFilterComboBox.setValue(ALL_STATUSES);
    }

    private void loadDoctors(String successMessage) {
        if (successMessage == null) {
            clearMessage(listMessageLabel);
        }

        BackgroundTasks.run(
                () -> authService.findDoctors(searchFilterField.getText(), selectedActiveFilter()),
                loadedDoctors -> {
                    doctors.setAll(loadedDoctors);
                    updateTableHeight();
                    if (successMessage != null) {
                        showSuccess(listMessageLabel, successMessage);
                    }
                },
                exception -> showListFailure(exception),
                () -> {
                }
        );
    }

    private Boolean selectedActiveFilter() {
        String value = statusFilterComboBox.getValue();
        if (ACTIVE_STATUS.equals(value)) {
            return true;
        }
        if (INACTIVE_STATUS.equals(value)) {
            return false;
        }
        return null;
    }

    private void updateTableHeight() {
        int rowCount = doctors.size();
        double tableHeight = rowCount == 0
                ? EMPTY_TABLE_HEIGHT
                : TABLE_HEADER_HEIGHT + (rowCount * TABLE_ROW_HEIGHT) + 2;
        doctorsTable.setMinHeight(tableHeight);
        doctorsTable.setPrefHeight(tableHeight);
        doctorsTable.setMaxHeight(tableHeight);
    }

    private void openCreateDialog() {
        editingDoctor = null;
        doctorDialogTitleLabel.setText("Create doctor account");
        doctorDialogSubtitleLabel.setText("Doctor accounts can sign in after an administrator creates them.");
        passwordField.setPromptText("Password");
        confirmPasswordField.setPromptText("Confirm password");
        createDoctorButton.setText("Create Doctor");
        clearCreateForm();
        showDoctorDialog();
    }

    private void openEditDialog(User doctor) {
        editingDoctor = doctor;
        doctorDialogTitleLabel.setText("Edit doctor account");
        doctorDialogSubtitleLabel.setText("Update account details. Leave password blank to keep the current password.");
        fullNameField.setText(safeText(doctor.getFullName()));
        emailField.setText(safeText(doctor.getEmail()));
        passwordField.clear();
        confirmPasswordField.clear();
        passwordField.setPromptText("New password (optional)");
        confirmPasswordField.setPromptText("Confirm new password");
        activeCheckBox.setSelected(doctor.isActive());
        createDoctorButton.setText("Save Changes");
        clearMessage(createMessageLabel);
        showDoctorDialog();
    }

    private void showDoctorDialog() {
        Parent parent = createDoctorDialog.getParent();
        if (parent instanceof Pane pane) {
            pane.getChildren().remove(createDoctorDialog);
        }
        ViewNavigator.showGlobalModal(createDoctorDialog);
        fullNameField.requestFocus();
    }

    private User saveDoctor() {
        if (editingDoctor == null) {
            return authService.createDoctor(
                    fullNameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    confirmPasswordField.getText(),
                    activeCheckBox.isSelected()
            );
        }

        return authService.updateDoctor(
                editingDoctor.getId(),
                fullNameField.getText(),
                emailField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                activeCheckBox.isSelected()
        );
    }

    private void handleDoctorSaved(User doctor) {
        boolean updated = editingDoctor != null;
        hideCreateDoctorModal();
        loadDoctors(updated
                ? "Doctor account updated for " + doctor.getEmail() + "."
                : "Doctor account created for " + doctor.getEmail() + ".");
    }

    private void confirmDeleteDoctor(User doctor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete doctor");
        alert.setHeaderText(null);
        alert.setContentText("Delete doctor account for " + doctor.getEmail() + "?");
        Optional<ButtonType> response = alert.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) {
            return;
        }

        clearMessage(listMessageLabel);
        BackgroundTasks.run(
                () -> {
                    authService.deleteDoctor(doctor.getId());
                    return doctor;
                },
                deletedDoctor -> loadDoctors("Doctor account deleted for " + deletedDoctor.getEmail() + "."),
                this::showDeleteFailure,
                () -> {
                }
        );
    }

    private void showCreateFailure(Throwable exception) {
        if (exception instanceof ValidationException || exception instanceof SecurityException) {
            showError(createMessageLabel, exception.getMessage());
        } else if (exception instanceof RepositoryException) {
            showError(createMessageLabel, "Doctor account could not be created. Check the database connection.");
        } else {
            showError(createMessageLabel, "Doctor account could not be created.");
        }
    }

    private void showListFailure(Throwable exception) {
        if (exception instanceof SecurityException) {
            showError(listMessageLabel, exception.getMessage());
        } else if (exception instanceof RepositoryException) {
            showError(listMessageLabel, "Doctors could not be loaded. Check the database connection.");
        } else {
            showError(listMessageLabel, "Doctors could not be loaded.");
        }
    }

    private void showDeleteFailure(Throwable exception) {
        if (exception instanceof ValidationException || exception instanceof SecurityException) {
            showError(listMessageLabel, exception.getMessage());
        } else if (exception instanceof RepositoryException) {
            showError(listMessageLabel, "Doctor could not be deleted. Remove related records first or check the database connection.");
        } else {
            showError(listMessageLabel, "Doctor could not be deleted.");
        }
    }

    private void hideCreateDoctorModal() {
        ViewNavigator.hideGlobalModal();
        createDoctorDialog.setVisible(false);
        createDoctorDialog.setManaged(false);
        editingDoctor = null;
    }

    private void clearCreateForm() {
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        activeCheckBox.setSelected(true);
        clearMessage(createMessageLabel);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
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

    private final class ActionCell extends TableCell<User, Void> {
        private final Button editButton = createActionButton("edit.svg", "Edit doctor");
        private final Button deleteButton = createActionButton("trash.svg", "Delete doctor");
        private final HBox actions = new HBox(10, editButton, deleteButton);

        private ActionCell() {
            actions.setAlignment(Pos.CENTER);
            editButton.setOnAction(event -> {
                User doctor = getTableRow().getItem();
                if (doctor != null) {
                    openEditDialog(doctor);
                }
            });
            deleteButton.setOnAction(event -> {
                User doctor = getTableRow().getItem();
                if (doctor != null) {
                    confirmDeleteDoctor(doctor);
                }
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : actions);
        }

        private Button createActionButton(String iconFileName, String accessibleText) {
            Node icon = AssetLoader.icon(iconFileName, 16);
            Button button = new Button();
            button.setGraphic(icon);
            button.setAccessibleText(accessibleText);
            button.setMnemonicParsing(false);
            button.getStyleClass().add("table-action-button");
            return button;
        }
    }
}
