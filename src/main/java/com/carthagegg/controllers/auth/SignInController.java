package com.carthagegg.controllers.auth;

import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.User;
import com.carthagegg.utils.GoogleAuthService;
import com.carthagegg.utils.SceneNavigator;
import com.carthagegg.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class SignInController {

    @FXML private ImageView logoImageView;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();
    private static final String PREF_REMEMBER_ME = "remember_me";
    private static final String PREF_EMAIL = "remembered_email";
    private final Preferences prefs = Preferences.userNodeForPackage(SignInController.class);

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        
        // Load logo
        try {
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/zz.png")));
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        boolean remember = prefs.getBoolean(PREF_REMEMBER_ME, false);
        if (remember) {
            String rememberedEmail = prefs.get(PREF_EMAIL, "");
            if (rememberedEmail != null && !rememberedEmail.isBlank()) {
                emailField.setText(rememberedEmail);
            }
            rememberMeCheckbox.setSelected(true);
        }
    }

    @FXML
    private void handleSignIn() {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }

        try {
            User user = userDAO.findByEmail(email);
            if (user != null && BCrypt.checkpw(password, user.getPassword())) {
                
                // Check if banned
                if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                    showBanAlert(user);
                    return;
                }

                // Check if active
                if (!user.isActive()) {
                    showError("Account is inactive");
                    return;
                }

                // Successful login
                SessionManager.setCurrentUser(user);

                if (rememberMeCheckbox != null && rememberMeCheckbox.isSelected()) {
                    prefs.putBoolean(PREF_REMEMBER_ME, true);
                    prefs.put(PREF_EMAIL, email);
                } else {
                    prefs.putBoolean(PREF_REMEMBER_ME, false);
                    prefs.remove(PREF_EMAIL);
                }
                try {
                    prefs.flush();
                } catch (BackingStoreException ignored) {}
                
                if (user.isAdmin()) {
                    SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml");
                } else {
                    SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
                }
            } else {
                showError("Invalid email or password");
            }
        } catch (Exception e) {
            showError("System error: " + e.getMessage());
            e.printStackTrace();
            
            // Show alert for critical errors like DB connection
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Database connection failed");
            alert.setContentText("Please ensure MariaDB is running and the database 'carthage_gg' exists.\n\nError: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showBanAlert(User user) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Account Banned");
        alert.setHeaderText("Your account has been banned until " + user.getBannedUntil().toString());
        alert.setContentText("Reason: " + user.getBanReason());
        alert.showAndWait();
    }

    @FXML
    private void handleGoogleSignIn() {
        GoogleAuthService.authenticate(new GoogleAuthService.GoogleAuthCallback() {
            @Override
            public void onSuccess(GoogleAuthService.GoogleUser gUser) {
                Platform.runLater(() -> {
                    try {
                        // 1. Try to find user by Google ID
                        User user = userDAO.findByGoogleId(gUser.id);
                        
                        if (user == null) {
                            // 2. Fallback: try by email (if they registered manually before)
                            user = userDAO.findByEmail(gUser.email);
                            if (user != null) {
                                // Link Google ID to existing account
                                user.setGoogleId(gUser.id);
                                userDAO.linkGoogleId(user.getUserId(), gUser.id);
                            }
                        }

                        if (user == null) {
                            // 3. Register new user
                            user = new User();
                            user.setEmail(gUser.email);
                            user.setGoogleId(gUser.id);
                            // Default password for google users (can't be logged in normally)
                            user.setPassword(BCrypt.hashpw(java.util.UUID.randomUUID().toString(), BCrypt.gensalt()));
                            user.setRoles("[\"ROLE_USER\"]");
                            
                            // Generate unique username
                            String baseUsername = gUser.email.split("@")[0];
                            user.setUsername(baseUsername + "_" + System.currentTimeMillis() % 1000);
                            
                            user.setFirstName(gUser.givenName != null ? gUser.givenName : "");
                            user.setLastName(gUser.familyName != null ? gUser.familyName : "");
                            user.setAvatar(gUser.picture);
                            user.setActive(true); // Ensure user is marked active in memory
                            
                            userDAO.save(user);
                        }

                        if (user != null) {
                            // Ensure user is active if they just signed in via Google
                            if (!user.isActive()) {
                                user.setActive(true);
                                userDAO.activateUser(user.getUserId());
                            }
                        }

                        // Check if banned
                        if (user.getBannedUntil() != null && user.getBannedUntil().isAfter(LocalDateTime.now())) {
                            showBanAlert(user);
                            return;
                        }

                        // Check if active (now redundant but good to keep)
                        if (!user.isActive()) {
                            showError("Account is inactive");
                            return;
                        }

                        // Successful login
                        SessionManager.setCurrentUser(user);
                        
                        if (user.isAdmin()) {
                            SceneNavigator.navigateTo("/com/carthagegg/fxml/back/AdminDashboard.fxml");
                        } else {
                            SceneNavigator.navigateTo("/com/carthagegg/fxml/front/Home.fxml");
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                        showError("Database error during Google Login.");
                        
                        // Show detailed alert for connection failures
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Connection Error");
                        alert.setHeaderText("Database connection failed during Google Login");
                        alert.setContentText("Please ensure MariaDB is running and the database 'carthage_gg' exists.\n\nError: " + e.getMessage());
                        alert.showAndWait();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Platform.runLater(() -> showError(error));
            }
        });
    }

    @FXML
    private void handleGoToSignUp() {
        SceneNavigator.navigateTo("/com/carthagegg/fxml/auth/SignUp.fxml");
    }
}
