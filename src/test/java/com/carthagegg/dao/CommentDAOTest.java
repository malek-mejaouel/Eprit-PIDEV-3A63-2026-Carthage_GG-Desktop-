package com.carthagegg.dao;

import com.carthagegg.models.Comment;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentDAOTest {

    private static CommentDAO service = new CommentDAO();
    private static int idCommentTest;

    @Test
    @Order(1)
    void testAjouterCommentaire() throws SQLException {
        Comment c = new Comment();
        c.setContenu("Test Comment Content");
        c.setNewsId(1); // Assumes a news with ID 1 exists, or doesn't matter for pure DAO test
        c.setUserId(1); // Assumes a user with ID 1 exists
        c.setUpvotes(0);
        c.setDownvotes(0);
        
        service.save(c);
        idCommentTest = c.getCommentaireId();
        
        List<Comment> comments = service.findAll();
        assertFalse(comments.isEmpty());
        
        assertTrue(
            comments.stream().anyMatch(comm -> 
                comm.getCommentaireId() == idCommentTest)
        );
    }

    @Test
    @Order(2)
    void testModifierCommentaire() throws SQLException {
        Comment c = new Comment();
        c.setCommentaireId(idCommentTest);
        c.setContenu("Commentaire Modifie");
        
        service.update(c);
        
        List<Comment> comments = service.findAll();
        boolean trouve = comments.stream()
            .anyMatch(comm -> 
                comm.getContenu().equals("Commentaire Modifie"));
        assertTrue(trouve);
    }

    @Test
    @Order(3)
    void testSupprimerCommentaire() throws SQLException {
        service.delete(idCommentTest);
        
        List<Comment> comments = service.findAll();
        boolean existe = comments.stream().anyMatch(c -> c.getCommentaireId() == idCommentTest);
        assertFalse(existe);
    }
}
