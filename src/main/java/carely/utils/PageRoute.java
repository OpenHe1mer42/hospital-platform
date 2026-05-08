package carely.utils;

public enum PageRoute {
    DASHBOARD("Dashboard", "home.svg", "/views/pages/dashboard.fxml", true),
    APPOINTMENTS("Appointments", "month.svg", null, true),
    PATIENTS("Patients", "patients.svg", null, true),
    PRESCRIPTIONS("Prescriptions", "medication.svg", null, true),
    MESSAGES("Messages", "inbox.svg", null, true),
    SETTINGS("Settings", "settings.svg", null, true),
    PROFILE("Profile", null, "/views/pages/profile.fxml", false);

    private final String title;
    private final String iconFileName;
    private final String fxmlPath;
    private final boolean sidebarVisible;

    PageRoute(String title, String iconFileName, String fxmlPath, boolean sidebarVisible) {
        this.title = title;
        this.iconFileName = iconFileName;
        this.fxmlPath = fxmlPath;
        this.sidebarVisible = sidebarVisible;
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

    public boolean isSidebarVisible() {
        return sidebarVisible;
    }
}
