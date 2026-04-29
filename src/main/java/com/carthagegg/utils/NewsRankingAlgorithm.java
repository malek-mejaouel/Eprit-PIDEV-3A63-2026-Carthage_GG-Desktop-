package com.carthagegg.utils;

import com.carthagegg.models.News;
import java.time.Duration;
import java.time.LocalDateTime;

public class NewsRankingAlgorithm {
    /**
     * score = (view_count * 1.0) + (comment_count * 2.0) + (total_upvotes * 1.5) / (age_in_hours + 2)^1.2
     */
    public static double calculateScore(News news) {
        double views = news.getViewCount() * 1.0;
        double comments = news.getCommentCount() * 2.0;
        double upvotes = news.getTotalUpvotes() * 1.5;
        
        long ageInHours = 0;
        if (news.getPublishedAt() != null) {
            ageInHours = Duration.between(news.getPublishedAt(), LocalDateTime.now()).toHours();
        }
        
        // Ensure age is not negative
        if (ageInHours < 0) ageInHours = 0;

        double numerator = views + comments + upvotes;
        double denominator = Math.pow(ageInHours + 2, 1.2);
        
        return numerator / denominator;
    }
}
