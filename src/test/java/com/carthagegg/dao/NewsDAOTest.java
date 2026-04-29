package com.carthagegg.dao;

import com.carthagegg.models.News;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NewsDAOTest {

    private static NewsDAO service = new NewsDAO();
    private static int idNewsTest;

    @Test
    @Order(1)
    void testAjouterNews() throws SQLException {
        News n = new News();
        n.setTitle("Test Title");
        n.setContent("Test Content exceeds 5 chars");
        n.setCategory("Tournaments");
        n.setImage("test.jpg");
        
        service.save(n);
        idNewsTest = n.getNewsId();
        
        List<News> newsList = service.findAll();
        assertFalse(newsList.isEmpty());
        
        assertTrue(
            newsList.stream().anyMatch(news -> 
                news.getNewsId() == idNewsTest)
        );
    }

    @Test
    @Order(2)
    void testModifierNews() throws SQLException {
        News n = new News();
        n.setNewsId(idNewsTest);
        n.setTitle("Title Modifie");
        n.setContent("Content Modifie exceeds 5 chars");
        n.setCategory("Esports");
        n.setImage("test_mod.jpg");
        
        service.update(n);
        
        List<News> newsList = service.findAll();
        boolean trouve = newsList.stream()
            .anyMatch(news -> 
                news.getTitle().equals("Title Modifie"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerNews() throws SQLException {
        service.delete(idNewsTest);
        
        List<News> newsList = service.findAll();
        boolean existe = newsList.stream().anyMatch(n -> n.getNewsId() == idNewsTest);
        assertFalse(existe);
    }
}
