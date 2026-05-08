package carely.service;

import carely.model.User;
import carely.model.UserRole;

import java.util.Arrays;
import java.util.Optional;

public final class AuthSession {
    private static User currentUser;

    private AuthSession() {
    }

    public static void start(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    public static Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public static boolean hasAnyRole(UserRole... roles) {
        return currentUser != null && Arrays.stream(roles).anyMatch(role -> currentUser.getRole() == role);
    }

    public static void requireAnyRole(UserRole... roles) {
        if (!hasAnyRole(roles)) {
            throw new SecurityException("You do not have permission to access this area.");
        }
    }
}
