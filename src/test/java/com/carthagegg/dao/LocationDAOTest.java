package com.carthagegg.dao;

import com.carthagegg.models.Location;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocationDAOTest {

    private LocationDAO service;
    private int idLocTest;

    @BeforeAll
    void setup() {
        service = new LocationDAO();
    }

    @Test
    @Order(1)
    void testAjouterLocation() throws SQLException {
        Location l = new Location();
        l.setName("Test Location");
        l.setAddress("123 Test Street");
        l.setCapacity(100);
        l.setLatitude(10.123);
        l.setLongitude(36.456);
        
        service.save(l);
        idLocTest = l.getId();
        
        List<Location> locations = service.findAll();
        assertFalse(locations.isEmpty());
        assertTrue(
            locations.stream().anyMatch(loc -> 
                loc.getName().equals("Test Location"))
        );
    }

    @Test
    @Order(2)
    void testModifierLocation() throws SQLException {
        Location l = service.findAll().stream()
                .filter(loc -> loc.getId() == idLocTest)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Location test non trouvée"));
        
        l.setName("Location Modifiée");
        l.setAddress("456 Modified Ave");
        l.setCapacity(200);
        
        service.update(l);
        
        List<Location> locations = service.findAll();
        boolean trouve = locations.stream()
            .anyMatch(loc -> 
                loc.getId() == idLocTest && loc.getName().equals("Location Modifiée"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerLocation() throws SQLException {
        service.delete(idLocTest);
        
        List<Location> locations = service.findAll();
        boolean existe = locations.stream().anyMatch(l -> l.getId() == idLocTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) throws SQLException {
        if (testInfo.getDisplayName().contains("testSupprimerLocation")) {
            List<Location> locations = service.findAll();
            if (!locations.isEmpty()) {
                locations.stream()
                    .filter(l -> l.getName().equals("Test Location") || l.getName().equals("Location Modifiée"))
                    .forEach(l -> {
                        try {
                            service.delete(l.getId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
            }
        }
    }
}
