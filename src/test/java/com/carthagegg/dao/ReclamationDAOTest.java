package com.carthagegg.dao;

import com.carthagegg.models.Reclamation;
import com.carthagegg.models.User;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReclamationDAOTest {

    private ReclamationDAO service;
    private UserDAO userService;
    private int idReclamationTest;
    private int idUserTest;

    @BeforeAll
    void setup() throws SQLException {
        service = new ReclamationDAO();
        userService = new UserDAO();
        
        // On crée un utilisateur pour attacher la réclamation
        User u = new User();
        u.setUsername("TestUserRec");
        u.setEmail("testrec@example.com");
        u.setPassword("password123");
        u.setRoles("[\"ROLE_USER\"]");
        u.setFirstName("Test");
        u.setLastName("Nom");
        userService.save(u);
        idUserTest = u.getUserId();
    }

    @AfterAll
    void tearDown() throws SQLException {
        userService.delete(idUserTest);
    }

    @Test
    @Order(1)
    void testAjouterReclamation() throws SQLException {
        Reclamation r = new Reclamation();
        r.setUserId(idUserTest);
        r.setTitle("TestTitre");
        r.setDescription("TestDescription");
        
        service.save(r);
        idReclamationTest = r.getId();
        
        List<Reclamation> reclamations = service.findAll();
        assertFalse(reclamations.isEmpty());
        assertTrue(
            reclamations.stream().anyMatch(rec -> 
                rec.getTitle().equals("TestTitre"))
        );
    }

    @Test
    @Order(2)
    void testModifierReclamation() throws SQLException {
        service.updateStatus(idReclamationTest, "resolved");
        
        List<Reclamation> reclamations = service.findAll();
        boolean trouve = reclamations.stream()
            .anyMatch(rec -> 
                rec.getId() == idReclamationTest && "resolved".equalsIgnoreCase(rec.getStatus()));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerReclamation() throws SQLException {
        service.delete(idReclamationTest);
        
        List<Reclamation> reclamations = service.findAll();
        boolean existe = reclamations.stream().anyMatch(r -> r.getId() == idReclamationTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) throws SQLException {
        if (testInfo.getDisplayName().contains("testSupprimerReclamation")) {
            List<Reclamation> reclamations = service.findAll();
            if (!reclamations.isEmpty()) {
                reclamations.stream()
                    .filter(r -> r.getTitle().equals("TestTitre"))
                    .forEach(r -> {
                        try {
                            service.delete(r.getId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
            }
        }
    }
}
