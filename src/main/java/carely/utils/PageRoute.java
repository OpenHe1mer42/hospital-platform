package carely.utils;

import carely.model.UserRole;

public enum PageRoute {
    DASHBOARD("Dashboard", "home.svg", "/views/pages/dashboard.fxml", true),
    DOCTORS("Doctors", "patients.svg", "/views/pages/doctors.fxml", true, UserRole.ADMIN),
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
    private final UserRole[] allowedRoles;

    PageRoute(String title, String iconFileName, String fxmlPath, boolean sidebarVisible) {
        this(title, iconFileName, fxmlPath, sidebarVisible, new UserRole[0]);
    }

    PageRoute(String title, String iconFileName, String fxmlPath, boolean sidebarVisible, UserRole... allowedRoles) {
        this.title = title;
        this.iconFileName = iconFileName;
        this.fxmlPath = fxmlPath;
        this.sidebarVisible = sidebarVisible;
        this.allowedRoles = allowedRoles;
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

    public boolean isRestricted() {
        return allowedRoles.length > 0;
    }

    public UserRole[] getAllowedRoles() {
        return allowedRoles;
    }
}
