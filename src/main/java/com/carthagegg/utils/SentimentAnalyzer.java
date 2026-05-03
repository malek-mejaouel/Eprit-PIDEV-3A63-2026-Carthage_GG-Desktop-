package com.carthagegg.utils;

import java.util.Arrays;
import java.util.List;

public class SentimentAnalyzer {

    // Keywords that indicate high urgency or aggressive mood
    private static final List<String> URGENT_KEYWORDS = Arrays.asList(
        "scam", "stole", "broken", "lost money", "hacked", "fraud", 
        "emergency", "urgent", "error", "failed", "danger", "fix now",
        "worst", "terrible", "disgusting", "angry", "lawsuit", "legal"
    );

    /**
     * Analyzes the description and returns a priority level.
     * @param text The reclamation description
     * @return "high" if urgent keywords are found, otherwise "normal"
     */
    public static String analyzePriority(String text) {
        if (text == null || text.isBlank()) return "normal";

        String lowerText = text.toLowerCase();
        
        // 1. Keyword detection
        for (String keyword : URGENT_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                return "high";
            }
        }

        // 2. All caps detection (indicates shouting/urgency)
        if (text.length() > 10 && text.equals(text.toUpperCase())) {
            return "high";
        }

        // 3. Excessive punctuation detection (!!! or ???)
        if (text.contains("!!!") || text.contains("???")) {
            return "high";
        }

        return "normal";
    }
}
