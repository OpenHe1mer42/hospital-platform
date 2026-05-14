package carely.service;

import carely.error.AuthenticationException;
import carely.error.ValidationException;
import carely.model.User;
import carely.repository.UserRepository;
import carely.utils.PasswordUtil;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

public class ProfileService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;

    public ProfileService() {
        this(new UserRepository());
    }

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User loadCurrentUser() {
        User currentUser = requireCurrentUser();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AuthenticationException("Your account could not be found."));
        AuthSession.start(user);
        return user;
    }

    public User updateProfile(String fullName, String email, String phone, String gender, LocalDate dateOfBirth) {
        User currentUser = requireCurrentUser();
        String normalizedFullName = normalizeText(fullName);
        String normalizedEmail = normalizeEmail(email);
        String normalizedPhone = normalizeOptionalText(phone);
        String normalizedGender = normalizeOptionalText(gender);

        requireNotBlank(normalizedFullName, "Full name is required.");
        requireValidEmail(normalizedEmail);
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future.");
        }
        if (userRepository.existsByEmailForOtherUser(normalizedEmail, currentUser.getId())) {
            throw new ValidationException("Another account already uses this email.");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AuthenticationException("Your account could not be found."));
        user.setFullName(normalizedFullName);
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setGender(normalizedGender);
        user.setDateOfBirth(dateOfBirth);

        User updatedUser = userRepository.updateProfile(user);
        AuthSession.start(updatedUser);
        return updatedUser;
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        User currentUser = requireCurrentUser();
        requireNotBlank(currentPassword, "Current password is required.");
        requireStrongPassword(newPassword);
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AuthenticationException("Your account could not be found."));
        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }

        userRepository.updatePassword(user.getId(), PasswordUtil.hashPassword(newPassword));
        AuthSession.start(userRepository.findById(user.getId())
                .orElseThrow(() -> new AuthenticationException("Your account could not be found.")));
    }

    public void deactivateCurrentAccount(String confirmationText) {
        User currentUser = requireCurrentUser();
        if (!"DEACTIVATE".equals(confirmationText == null ? "" : confirmationText.trim())) {
            throw new ValidationException("Type DEACTIVATE to confirm account deactivation.");
        }

        userRepository.deactivate(currentUser.getId());
        AuthSession.clear();
    }

    private User requireCurrentUser() {
        return AuthSession.getCurrentUser()
                .orElseThrow(() -> new AuthenticationException("Please log in again to manage your profile."));
    }

    private void requireValidEmail(String email) {
        requireNotBlank(email, "Email address is required.");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Enter a valid email address.");
        }
    }

    private void requireStrongPassword(String password) {
        requireNotBlank(password, "New password is required.");
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be at least 8 characters.");
        }
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeOptionalText(String value) {
        String normalized = normalizeText(value);
        return normalized.isBlank() ? null : normalized;
    }
}
