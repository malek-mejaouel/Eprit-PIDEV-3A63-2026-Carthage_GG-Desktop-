package com.carthagegg.dao;

import com.carthagegg.models.Category;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CategoryDAOTest {

    private CategoryDAO service;
    private int idCategoryTest;

    @BeforeAll
    void setup() {
        service = new CategoryDAO();
    }

    @Test
    @Order(1)
    void testAjouterCategory() throws SQLException {
        Category c = new Category();
        c.setName("Test Category");
        c.setDescription("Test Description");
        
        service.save(c);
        idCategoryTest = c.getId();
        
        List<Category> categories = service.findAll();
        assertFalse(categories.isEmpty());
        assertTrue(
            categories.stream().anyMatch(cat -> 
                cat.getName().equals("Test Category"))
        );
    }

    @Test
    @Order(2)
    void testModifierCategory() throws SQLException {
        Category c = service.findAll().stream()
                .filter(cat -> cat.getId() == idCategoryTest)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Category test non trouvé"));
        
        c.setName("Category Modifie");
        c.setDescription("Description Modifiee");
        
        service.update(c);
        
        List<Category> categories = service.findAll();
        boolean trouve = categories.stream()
            .anyMatch(cat -> 
                cat.getId() == idCategoryTest && cat.getName().equals("Category Modifie"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerCategory() throws SQLException {
        service.delete(idCategoryTest);
        
        List<Category> categories = service.findAll();
        boolean existe = categories.stream().anyMatch(c -> c.getId() == idCategoryTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) throws SQLException {
        if (testInfo.getDisplayName().contains("testSupprimerCategory")) {
            List<Category> categories = service.findAll();
            if (!categories.isEmpty()) {
                categories.stream()
                    .filter(c -> c.getName().equals("Test Category") || c.getName().equals("Category Modifie"))
                    .forEach(c -> {
                        try {
                            service.delete(c.getId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
            }
        }
    }
}
