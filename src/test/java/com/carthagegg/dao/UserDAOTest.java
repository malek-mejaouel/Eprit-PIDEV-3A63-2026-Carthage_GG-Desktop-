package com.carthagegg.dao;

import com.carthagegg.models.User;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDAOTest {

    private UserDAO service;
    private int idUserTest;

    @BeforeAll
    void setup() {
        service = new UserDAO();
    }

    @Test
    @Order(1)
    void testAjouterUser() throws SQLException {
        User u = new User();
        u.setUsername("TestUser");
        u.setEmail("testuser@example.com");
        u.setPassword("password123");
        u.setRoles("[\"ROLE_USER\"]");
        u.setFirstName("Test");
        u.setLastName("Nom");
        
        service.save(u);
        idUserTest = u.getUserId();
        
        List<User> users = service.findAll();
        assertFalse(users.isEmpty());
        assertTrue(
            users.stream().anyMatch(user -> 
                user.getUsername().equals("TestUser"))
        );
    }

    @Test
    @Order(2)
    void testModifierUser() throws SQLException {
        User u = service.findAll().stream()
                .filter(user -> user.getUserId() == idUserTest)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User test non trouvé"));
        
        u.setUsername("UserModifie");
        u.setFirstName("PrenomModifie");
        u.setLastName("NomModifie");
        u.setAvatar("new_avatar.png");
        
        service.update(u);
        
        List<User> users = service.findAll();
        boolean trouve = users.stream()
            .anyMatch(user -> 
                user.getUserId() == idUserTest && user.getUsername().equals("UserModifie"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerUser() throws SQLException {
        service.delete(idUserTest);
        
        List<User> users = service.findAll();
        boolean existe = users.stream().anyMatch(u -> u.getUserId() == idUserTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) throws SQLException {
        // On ne nettoie qu'après le dernier test ou si on veut vraiment isoler
        if (testInfo.getTags().contains("cleanup") || testInfo.getDisplayName().contains("testSupprimerUser")) {
            List<User> users = service.findAll();
            if (!users.isEmpty()) {
                users.stream()
                    .filter(u -> u.getUsername().equals("TestUser") || u.getUsername().equals("UserModifie"))
                    .forEach(u -> {
                        try {
                            service.delete(u.getUserId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
            }
        }
    }
}
