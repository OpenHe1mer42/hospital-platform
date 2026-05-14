package carely.service;

import carely.model.User;
import carely.model.UserRole;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuthSession {
    private static User currentUser;
    private static final List<WeakReference<Runnable>> changeListeners = new CopyOnWriteArrayList<>();

    private AuthSession() {
    }

    public static void start(User user) {
        currentUser = user;
        notifyChangeListeners();
    }

    public static void clear() {
        currentUser = null;
        notifyChangeListeners();
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

    public static void addChangeListener(Runnable listener) {
        if (listener != null) {
            changeListeners.add(new WeakReference<>(listener));
        }
    }

    private static void notifyChangeListeners() {
        changeListeners.removeIf(listenerReference -> {
            Runnable listener = listenerReference.get();
            if (listener == null) {
                return true;
            }
            listener.run();
            return false;
        });
    }
}
