package carely.service;

import carely.error.AuthenticationException;
import carely.error.ValidationException;
import carely.model.User;
import carely.model.UserRole;
import carely.repository.UserRepository;
import carely.utils.PasswordUtil;

import java.util.Locale;
import java.util.List;
import java.util.regex.Pattern;

public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;

    public AuthService() {
        this(new UserRepository());
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        requireNotBlank(normalizedEmail, "Email address is required.");
        requireNotBlank(password, "Password is required.");

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password."));

        if (!user.isActive()) {
            throw new AuthenticationException("This account is inactive. Contact an administrator.");
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        return user;
    }

    public User signup(String fullName, String email, String password, String confirmPassword) {
        String normalizedFullName = normalizeText(fullName);
        String normalizedEmail = normalizeEmail(email);

        requireNotBlank(normalizedFullName, "Full name is required.");
        requireValidEmail(normalizedEmail);
        requireStrongPassword(password);
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ValidationException("An account with this email already exists.");
        }

        User user = new User();
        user.setFullName(normalizedFullName);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setRole(UserRole.PATIENT);
        user.setActive(true);
        return userRepository.create(user);
    }

    public User createDoctor(String fullName, String email, String password, String confirmPassword, boolean active) {
        AuthSession.requireAnyRole(UserRole.ADMIN);

        String normalizedFullName = normalizeText(fullName);
        String normalizedEmail = normalizeEmail(email);

        requireNotBlank(normalizedFullName, "Full name is required.");
        requireValidEmail(normalizedEmail);
        requireStrongPassword(password);
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ValidationException("An account with this email already exists.");
        }

        User user = new User();
        user.setFullName(normalizedFullName);
        user.setEmail(normalizedEmail);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setRole(UserRole.DOCTOR);
        user.setActive(active);
        return userRepository.create(user);
    }

    public List<User> findDoctors(String searchTerm, Boolean active) {
        AuthSession.requireAnyRole(UserRole.ADMIN);
        return userRepository.findByRole(UserRole.DOCTOR, normalizeText(searchTerm), active);
    }

    public User updateDoctor(Long doctorId, String fullName, String email, String password, String confirmPassword, boolean active) {
        AuthSession.requireAnyRole(UserRole.ADMIN);

        if (doctorId == null) {
            throw new ValidationException("Doctor ID is required.");
        }

        User existingDoctor = userRepository.findById(doctorId)
                .filter(user -> user.getRole() == UserRole.DOCTOR)
                .orElseThrow(() -> new ValidationException("Doctor could not be found."));

        String normalizedFullName = normalizeText(fullName);
        String normalizedEmail = normalizeEmail(email);

        requireNotBlank(normalizedFullName, "Full name is required.");
        requireValidEmail(normalizedEmail);
        if (userRepository.existsByEmailForOtherUser(normalizedEmail, doctorId)) {
            throw new ValidationException("An account with this email already exists.");
        }

        String passwordHash = null;
        if (password != null && !password.isBlank()) {
            requireStrongPassword(password);
            if (!password.equals(confirmPassword)) {
                throw new ValidationException("Passwords do not match.");
            }
            passwordHash = PasswordUtil.hashPassword(password);
        } else if (confirmPassword != null && !confirmPassword.isBlank()) {
            throw new ValidationException("Password is required when confirmation is provided.");
        }

        existingDoctor.setFullName(normalizedFullName);
        existingDoctor.setEmail(normalizedEmail);
        existingDoctor.setActive(active);
        return userRepository.updateDoctor(existingDoctor, passwordHash);
    }

    public void deleteDoctor(Long doctorId) {
        AuthSession.requireAnyRole(UserRole.ADMIN);

        if (doctorId == null) {
            throw new ValidationException("Doctor ID is required.");
        }

        User existingDoctor = userRepository.findById(doctorId)
                .filter(user -> user.getRole() == UserRole.DOCTOR)
                .orElseThrow(() -> new ValidationException("Doctor could not be found."));
        userRepository.deleteDoctor(existingDoctor.getId());
    }

    private void requireValidEmail(String email) {
        requireNotBlank(email, "Email address is required.");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Enter a valid email address.");
        }
    }

    private void requireStrongPassword(String password) {
        requireNotBlank(password, "Password is required.");
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
}
