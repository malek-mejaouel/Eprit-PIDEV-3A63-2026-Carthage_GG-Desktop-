package com.carthagegg.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImageGenerator {

    private static final String OUTPUT_DIR = "uploads/certifications/";

    static {
        try {
            Files.createDirectories(Paths.get(OUTPUT_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simulates an AI generating a certification image for the user.
     * The image contains the app name, logo (text-based), user email, and role.
     */
    public static String generateCertification(String email, String role) {
        int width = 600;
        int height = 400;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing for smoother text
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background: Dark gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(13, 13, 21), 0, height, new Color(26, 26, 46));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        // Border: Gold/Cyan neon
        g2d.setColor(new Color(0, 240, 255)); // Cyan
        g2d.setStroke(new BasicStroke(10));
        g2d.drawRect(5, 5, width - 10, height - 10);

        // App Name (CarthageGG)
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(new Color(255, 215, 0)); // Gold
        g2d.drawString("CarthageGG", 50, 70);

        // Logo Simulation (A stylized 'C')
        g2d.setColor(new Color(0, 240, 255));
        g2d.fillOval(width - 100, 40, 60, 60);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("C", width - 85, 85);

        // Certificate Title
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.setColor(Color.WHITE);
        g2d.drawString("OFFICIAL ROLE CERTIFICATION", 50, 130);

        // Divider
        g2d.setColor(new Color(113, 113, 122));
        g2d.drawLine(50, 150, width - 50, 150);

        // User Details
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("This is to certify that the user with email:", 50, 200);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(Color.WHITE);
        g2d.drawString(email, 50, 230);

        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("has been verified for the role of:", 50, 270);

        g2d.setFont(new Font("Arial", Font.BOLD, 26));
        g2d.setColor(new Color(0, 240, 255));
        g2d.drawString(role.toUpperCase(), 50, 305);

        // Date and Serial
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2d.setColor(new Color(113, 113, 122));
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        g2d.drawString("Issued: " + date, 50, 360);
        g2d.drawString("Serial: " + Long.toHexString(Double.doubleToLongBits(Math.random())).toUpperCase(), 50, 380);

        g2d.dispose();

        String fileName = "cert_" + System.currentTimeMillis() + ".png";
        File outputFile = new File(OUTPUT_DIR + fileName);
        try {
            ImageIO.write(image, "png", outputFile);
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
