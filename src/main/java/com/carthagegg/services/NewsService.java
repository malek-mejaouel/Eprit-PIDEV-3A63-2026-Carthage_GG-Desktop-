package com.carthagegg.services;

import com.carthagegg.dao.CommentDAO;
import com.carthagegg.dao.NewsDAO;
import com.carthagegg.models.News;
import com.carthagegg.utils.NewsRankingAlgorithm;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewsService {
    private final NewsDAO newsDAO;
    private final CommentDAO commentDAO;

    public NewsService() {
        this.newsDAO = new NewsDAO();
        this.commentDAO = new CommentDAO();
    }

    public List<News> getSortedNewsByScore() {
        try {
            List<News> allNews = newsDAO.findAll();
            for (News news : allNews) {
                // Fetch metrics
                int comments = commentDAO.getCommentCount(news.getNewsId());
                int upvotes = commentDAO.getTotalUpvotes(news.getNewsId());
                
                news.setCommentCount(comments);
                news.setTotalUpvotes(upvotes);
            }
            
            // Sort by calculated score descending
            allNews.sort((n1, n2) -> Double.compare(
                NewsRankingAlgorithm.calculateScore(n2),
                NewsRankingAlgorithm.calculateScore(n1)
            ));
            
            return allNews;
        } catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void incrementViewCount(int newsId) {
        newsDAO.incrementViewCount(newsId);
    }
}
