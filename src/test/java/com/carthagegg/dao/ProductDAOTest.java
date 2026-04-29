package com.carthagegg.dao;

import com.carthagegg.models.Product;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductDAOTest {

    private ProductDAO service;
    private int idProductTest;

    @BeforeAll
    void setup() {
        service = new ProductDAO();
    }

    @Test
    @Order(1)
    void testAjouterProduct() throws SQLException {
        Product p = new Product();
        p.setName("Test Product");
        p.setPrice(new BigDecimal("99.99"));
        p.setCategoryId(1); // Assuming category 1 exists
        p.setStock(50);
        p.setImage("test_image.png");
        
        service.save(p);
        idProductTest = p.getId();
        
        List<Product> products = service.findAll();
        assertFalse(products.isEmpty());
        assertTrue(
            products.stream().anyMatch(product -> 
                product.getName().equals("Test Product"))
        );
    }

    @Test
    @Order(2)
    void testModifierProduct() throws SQLException {
        Product p = service.findAll().stream()
                .filter(product -> product.getId() == idProductTest)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Product test non trouvé"));
        
        p.setName("Product Modifie");
        p.setPrice(new BigDecimal("79.99"));
        p.setStock(30);
        
        service.update(p);
        
        List<Product> products = service.findAll();
        boolean trouve = products.stream()
            .anyMatch(product -> 
                product.getId() == idProductTest && product.getName().equals("Product Modifie") && product.getPrice().compareTo(new BigDecimal("79.99")) == 0);
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerProduct() throws SQLException {
        service.delete(idProductTest);
        
        List<Product> products = service.findAll();
        boolean existe = products.stream().anyMatch(p -> p.getId() == idProductTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) throws SQLException {
        if (testInfo.getDisplayName().contains("testSupprimerProduct")) {
            List<Product> products = service.findAll();
            if (!products.isEmpty()) {
                products.stream()
                    .filter(p -> p.getName().equals("Test Product") || p.getName().equals("Product Modifie"))
                    .forEach(p -> {
                        try {
                            service.delete(p.getId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
            }
        }
    }
}
