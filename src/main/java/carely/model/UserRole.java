package carely.model;

public enum UserRole {
    ADMIN("Admin"),
    DOCTOR("Doctor"),
    STAFF("Staff"),
    PATIENT("Patient");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
