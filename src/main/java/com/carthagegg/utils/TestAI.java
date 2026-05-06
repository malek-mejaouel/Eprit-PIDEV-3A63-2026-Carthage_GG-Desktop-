package com.carthagegg.utils;

public class TestAI {
    public static void main(String[] args) {
        AIService ai = new AIService();
        try {
            System.out.println("Testing AI connection...");
            String response = ai.getAIResponse("Hello, are you working?");
            System.out.println("AI Response: " + response);
        } catch (Exception e) {
            System.err.println("Test failed!");
            e.printStackTrace();
        }
    }
}
