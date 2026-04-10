package com.carthagegg.controllers.auth;

import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.User;
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

    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Player", "Viewer", "Coach");
        roleComboBox.setValue("Player");
    }

    @FXML
    private void handleSignUp() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || 
            email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Error", "All fields are required", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (userDAO.findByEmail(email) != null) {
                showAlert("Error", "Email already registered", Alert.AlertType.ERROR);
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

            showAlert("Success", "Account created successfully!", Alert.AlertType.INFORMATION);
            SceneNavigator.navigateTo("/com/carthagegg/fxml/auth/SignIn.fxml");

        } catch (Exception e) {
            showAlert("Error", "System error: " + e.getMessage(), Alert.AlertType.ERROR);
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
