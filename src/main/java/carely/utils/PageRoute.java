package carely.utils;

public enum PageRoute {
    DASHBOARD("Dashboard", "home.svg", "/views/pages/dashboard.fxml"),
    APPOINTMENTS("Appointments", "month.svg", null),
    PATIENTS("Patients", "patients.svg", null),
    PRESCRIPTIONS("Prescriptions", "medication.svg", null),
    MESSAGES("Messages", "inbox.svg", null),
    SETTINGS("Settings", "settings.svg", null);

    private final String title;
    private final String iconFileName;
    private final String fxmlPath;

    PageRoute(String title, String iconFileName, String fxmlPath) {
        this.title = title;
        this.iconFileName = iconFileName;
        this.fxmlPath = fxmlPath;
    }

    public String getTitle() {
        return title;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
