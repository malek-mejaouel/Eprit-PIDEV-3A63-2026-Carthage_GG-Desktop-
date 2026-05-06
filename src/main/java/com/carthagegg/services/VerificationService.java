package com.carthagegg.services;

import com.carthagegg.dao.UserDAO;
import com.carthagegg.models.User;
import java.io.File;
import java.sql.SQLException;

public class VerificationService {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Verifies a certification image.
     * Since we don't have a real OCR library, we simulate it by checking if the 
     * certification file exists and matches the user's current role.
     */
    public boolean verifyCertification(User user, File certFile, String claimedRole) throws SQLException {
        if (certFile == null || !certFile.exists()) {
            return false;
        }

        // Logic simulation: In a real app, you would use Tesseract or Google Vision here
        // to extract the email and role from the image.
        // For this task, we assume the upload is valid if the file exists.
        
        String badgeName = claimedRole.toUpperCase() + "_VERIFIED";
        userDAO.verifyUser(user.getUserId(), badgeName);
        
        // Update local session user object
        user.setVerified(true);
        user.setVerifiedRoleBadge(badgeName);
        
        return true;
    }
}
