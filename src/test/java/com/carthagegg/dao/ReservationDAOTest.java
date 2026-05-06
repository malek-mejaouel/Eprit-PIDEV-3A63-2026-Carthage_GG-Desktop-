package com.carthagegg.dao;

import com.carthagegg.models.Event;
import com.carthagegg.models.Location;
import com.carthagegg.models.Reservation;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReservationDAOTest {

    private ReservationDAO resService;
    private EventDAO eventService;
    private LocationDAO locationService;
    
    private int idResTest;
    private int idEventTest;
    private int idLocationTest;

    @BeforeAll
    void setup() throws SQLException {
        resService = new ReservationDAO();
        eventService = new EventDAO();
        locationService = new LocationDAO();
        
        // Setup prerequisites
        Location l = new Location();
        l.setName("Res Test Location");
        l.setAddress("Res Test Address");
        l.setCapacity(100);
        locationService.save(l);
        idLocationTest = l.getId();
        
        Event e = new Event();
        e.setTitle("Res Test Event");
        e.setDescription("Res Test Description");
        e.setStartAt(LocalDateTime.now().plusDays(1));
        e.setLocationId(idLocationTest);
        e.setMaxSeats(50);
        eventService.save(e);
        idEventTest = e.getId();
    }

    @Test
    @Order(1)
    void testAjouterReservation() throws SQLException {
        Reservation r = new Reservation();
        r.setName("Test Reservation");
        r.setPrice(new BigDecimal("25.50"));
        r.setReservationDate(LocalDateTime.now());
        r.setEventId(idEventTest);
        r.setStatus(Reservation.Status.WAITING);
        
        resService.save(r);
        idResTest = r.getId();
        
        List<Reservation> reservations = resService.findAll();
        assertFalse(reservations.isEmpty());
        assertTrue(
            reservations.stream().anyMatch(res -> 
                res.getName().equals("Test Reservation"))
        );
    }

    @Test
    @Order(2)
    void testModifierReservation() throws SQLException {
        Reservation r = resService.findAll().stream()
                .filter(res -> res.getId() == idResTest)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Reservation test non trouvée"));
        
        r.setName("Reservation Modifiée");
        r.setStatus(Reservation.Status.CONFIRMED);
        
        resService.update(r);
        
        List<Reservation> reservations = resService.findAll();
        boolean trouve = reservations.stream()
            .anyMatch(res -> 
                res.getId() == idResTest && res.getName().equals("Reservation Modifiée") && res.getStatus() == Reservation.Status.CONFIRMED);
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerReservation() throws SQLException {
        resService.delete(idResTest);
        
        List<Reservation> reservations = resService.findAll();
        boolean existe = reservations.stream().anyMatch(r -> r.getId() == idResTest);
        assertFalse(existe);
    }

    @AfterAll
    void tearDown() throws SQLException {
        // Nettoyage final
        eventService.delete(idEventTest);
        locationService.delete(idLocationTest);
    }
}
