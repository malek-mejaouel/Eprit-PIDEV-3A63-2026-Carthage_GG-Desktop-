package com.carthagegg.dao;

import com.carthagegg.models.Event;
import com.carthagegg.models.Location;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventDAOTest {

    private EventDAO eventService;
    private LocationDAO locationService;
    private int idEventTest;
    private int idLocationTest;

    @BeforeAll
    void setup() throws SQLException {
        eventService = new EventDAO();
        locationService = new LocationDAO();
        
        // On crée une location pour le test de l'événement
        Location l = new Location();
        l.setName("Event Test Location");
        l.setAddress("Event Test Address");
        l.setCapacity(100);
        locationService.save(l);
        idLocationTest = l.getId();
    }

    @Test
    @Order(1)
    void testAjouterEvent() throws SQLException {
        Event e = new Event();
        e.setTitle("Test Event");
        e.setDescription("Test Description");
        e.setStartAt(LocalDateTime.now().plusDays(1));
        e.setEndAt(LocalDateTime.now().plusDays(1).plusHours(2));
        e.setLocationId(idLocationTest);
        e.setMaxSeats(50);
        
        eventService.save(e);
        idEventTest = e.getId();
        
        List<Event> events = eventService.findAll();
        assertFalse(events.isEmpty());
        assertTrue(
            events.stream().anyMatch(event -> 
                event.getTitle().equals("Test Event"))
        );
    }

    @Test
    @Order(2)
    void testModifierEvent() throws SQLException {
        Event e = eventService.findAll().stream()
                .filter(event -> event.getId() == idEventTest)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Event test non trouvé"));
        
        e.setTitle("Event Modifié");
        e.setDescription("Description Modifiée");
        
        eventService.update(e);
        
        List<Event> events = eventService.findAll();
        boolean trouve = events.stream()
            .anyMatch(event -> 
                event.getId() == idEventTest && event.getTitle().equals("Event Modifié"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerEvent() throws SQLException {
        eventService.delete(idEventTest);
        
        List<Event> events = eventService.findAll();
        boolean existe = events.stream().anyMatch(e -> e.getId() == idEventTest);
        assertFalse(existe);
    }

    @AfterAll
    void tearDown() throws SQLException {
        // Nettoyage final
        locationService.delete(idLocationTest);
    }
}
