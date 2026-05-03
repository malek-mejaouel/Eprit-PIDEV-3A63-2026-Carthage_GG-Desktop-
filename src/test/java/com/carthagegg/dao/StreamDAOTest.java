package com.carthagegg.dao;

import com.carthagegg.models.Stream;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StreamDAOTest {

    private StreamDAO service;
    private int idStreamTest;

    @BeforeAll
    void setup() {
        service = new StreamDAO();
    }

    @Test
    @Order(1)
    void testAjouterStream() throws SQLException {
        Stream s = new Stream();
        s.setTitle("Test Stream");
        s.setDescription("Test Description");
        s.setPlatform("twitch");
        s.setChannelName("TestChannel");
        s.setYoutubeVideoId("test_yt_id");
        s.setLive(true);
        s.setViewerCount(100);
        s.setCreatedBy(1); // Assuming user 1 exists or using a dummy ID
        
        service.save(s);
        idStreamTest = s.getStreamId();
        
        List<Stream> streams = service.findAll();
        assertFalse(streams.isEmpty());
        assertTrue(
            streams.stream().anyMatch(stream -> 
                stream.getTitle().equals("Test Stream"))
        );
    }

    @Test
    @Order(2)
    void testModifierStream() throws SQLException {
        Stream s = service.findAll().stream()
                .filter(stream -> stream.getStreamId() == idStreamTest)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Stream test non trouvé"));
        
        s.setTitle("Stream Modifie");
        s.setViewerCount(200);
        s.setLive(false);
        
        service.update(s);
        
        List<Stream> streams = service.findAll();
        boolean trouve = streams.stream()
            .anyMatch(stream -> 
                stream.getStreamId() == idStreamTest && stream.getTitle().equals("Stream Modifie") && stream.getViewerCount() == 200);
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerStream() throws SQLException {
        service.delete(idStreamTest);
        
        List<Stream> streams = service.findAll();
        boolean existe = streams.stream().anyMatch(s -> s.getStreamId() == idStreamTest);
        assertFalse(existe);
    }

    @AfterEach
    void cleanUp(TestInfo testInfo) throws SQLException {
        if (testInfo.getDisplayName().contains("testSupprimerStream")) {
            List<Stream> streams = service.findAll();
            if (!streams.isEmpty()) {
                streams.stream()
                    .filter(s -> s.getTitle().equals("Test Stream") || s.getTitle().equals("Stream Modifie"))
                    .forEach(s -> {
                        try {
                            service.delete(s.getStreamId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
            }
        }
    }
}
