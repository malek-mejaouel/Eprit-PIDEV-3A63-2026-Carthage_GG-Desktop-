package com.carthagegg.controllers.auth;

import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.User;
import com.carthagegg.utils.EmailService;
import com.carthagegg.utils.SceneNavigator;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SignUpController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Player", "Viewer", "Coach");
        roleComboBox.setValue("Player");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void handleSignUp() {
        clearError();

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        if (firstName.isEmpty()) {
            showError("First name is required");
            return;
        }
        if (lastName.isEmpty()) {
            showError("Last name is required");
            return;
        }
        if (username.isEmpty()) {
            showError("Username is required");
            return;
        }
        if (email.isEmpty()) {
            showError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            showError("Password is required");
            return;
        }
        if (confirmPassword.isEmpty()) {
            showError("Please confirm your password");
            return;
        }
        if (role == null || role.isEmpty()) {
            showError("Please select a role");
            return;
        }

        if (!email.contains("@")) {
            showError("Please enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        try {
            if (userDAO.findByEmail(email) != null) {
                showError("Email already registered");
                return;
            }

            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            
            // Map role to ROLE_ format
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_" + role.toUpperCase());
            user.setRoles(gson.toJson(roles));

            userDAO.save(user);

            // Send welcome email via n8n
            EmailService.sendWelcomeEmail(user.getEmail(), user.getFirstName() + " " + user.getLastName());

            showAlert("Success", "Account created successfully!", Alert.AlertType.INFORMATION);
            SceneNavigator.navigateTo("/com/carthagegg/fxml/auth/SignIn.fxml");

        } catch (Exception e) {
            showError("System error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleGoToSignIn() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/auth/SignIn.fxml");
    }
}
