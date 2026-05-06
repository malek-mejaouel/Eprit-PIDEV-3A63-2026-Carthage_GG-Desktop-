package com.carthagegg.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

/**
 * Unified EmailService — handles:
 *  1. SMTP emails with optional PDF attachments (reservation confirmations, etc.)
 *  2. n8n webhook triggers (welcome emails via automation)
 */
public class EmailService {

    // ─────────────────────────────────────────────
    // SMTP Configuration
    // ─────────────────────────────────────────────
    private static final String SMTP_HOST      = "smtp.gmail.com";
    private static final int    SMTP_PORT      = 587;
    private static final String SENDER_EMAIL   = "anasbouzid250@gmail.com";
    private static final String SENDER_PASSWORD = "phwnkuctnnejxkst";
    private static final String SENDER_NAME    = "CarthageGG";

    // ─────────────────────────────────────────────
    // n8n Webhook Configuration (loaded from config.properties)
    // ─────────────────────────────────────────────
    private static final Properties config = new Properties();
    private static final String WEBHOOK_URL;

    static {
        try (InputStream input = EmailService.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            }
        } catch (IOException ex) {
            System.err.println("Could not load config.properties: " + ex.getMessage());
        }
        WEBHOOK_URL = config.getProperty("n8n.signup_webhook", "");
    }

    // ═════════════════════════════════════════════
    // n8n WEBHOOK — Welcome Email
    // ═════════════════════════════════════════════

    /**
     * Triggers the n8n welcome email workflow asynchronously.
     *
     * @param email New user's email address
     * @param name  New user's display name
     */
    public static void sendWelcomeEmail(String email, String name) {
        if (WEBHOOK_URL.isEmpty()) {
            System.err.println("n8n webhook URL not configured in config.properties");
            return;
        }

        new Thread(() -> {
            try {
                String json = String.format("{\"email\": \"%s\", \"name\": \"%s\"}", email, name);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(WEBHOOK_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("✅ Welcome email trigger sent successfully to n8n");
                } else {
                    System.err.println("❌ Failed to trigger n8n webhook. Status: " + response.statusCode());
                    System.err.println("Response: " + response.body());
                }
            } catch (Exception e) {
                System.err.println("❌ Error triggering welcome email: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // ═════════════════════════════════════════════
    // SMTP — Core Send Methods
    // ═════════════════════════════════════════════

    /**
     * Sends an email with an optional PDF attachment via SMTP.
     *
     * @param recipientEmail Recipient's email address
     * @param subject        Email subject line
     * @param body           HTML email body
     * @param pdfFile        PDF file to attach, or null for no attachment
     * @return true if the email was sent successfully
     */
    public static boolean sendEmailWithPDF(String recipientEmail, String subject,
                                           String body, File pdfFile) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host",                SMTP_HOST);
            props.put("mail.smtp.port",                SMTP_PORT);
            props.put("mail.smtp.auth",                "true");
            props.put("mail.smtp.starttls.enable",     "true");
            props.put("mail.smtp.starttls.required",   "true");
            props.put("mail.smtp.connectiontimeout",   "5000");
            props.put("mail.smtp.timeout",             "5000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body, "utf-8", "html");
            multipart.addBodyPart(textPart);

            if (pdfFile != null && pdfFile.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(pdfFile);
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("✅ Email sent successfully to: " + recipientEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends a plain HTML email without an attachment.
     *
     * @param recipientEmail Recipient's email address
     * @param subject        Email subject line
     * @param body           HTML email body
     * @return true if the email was sent successfully
     */
    public static boolean sendEmail(String recipientEmail, String subject, String body) {
        return sendEmailWithPDF(recipientEmail, subject, body, null);
    }

    // ═════════════════════════════════════════════
    // SMTP — Reservation Confirmation
    // ═════════════════════════════════════════════

    /**
     * Sends a reservation confirmation email with a PDF ticket attached.
     *
     * @param recipientEmail Recipient's email address
     * @param recipientName  Recipient's display name
     * @param reservationId  Unique reservation ID
     * @param eventTitle     Name of the event
     * @param pdfFile        PDF ticket file to attach
     * @return true if the email was sent successfully
     */
    public static boolean sendReservationConfirmation(String recipientEmail, String recipientName,
                                                      int reservationId, String eventTitle,
                                                      File pdfFile) {
        String subject = "🎫 Votre billet de réservation - CarthageGG";

        String body = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #00f0ff; padding: 20px; text-align: center; color: white; border-radius: 5px; }
                        .content { padding: 20px; border: 1px solid #ddd; border-radius: 5px; margin-top: 10px; }
                        .footer { text-align: center; color: #999; margin-top: 20px; font-size: 12px; }
                        .highlight { color: #FFC107; font-weight: bold; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>🎮 CarthageGG - Confirmation de Réservation</h2>
                        </div>
                        <div class="content">
                            <p>Bonjour <span class="highlight">%s</span>,</p>
                            <p>Nous sommes heureux de confirmer votre réservation!</p>
                            <br/>
                            <p><strong>Détails de votre réservation:</strong></p>
                            <ul>
                                <li>Numéro de réservation: <span class="highlight">#%d</span></li>
                                <li>Événement: <span class="highlight">%s</span></li>
                            </ul>
                            <p>Votre billet PDF est joint à cet email. Veuillez le télécharger et le conserver.</p>
                            <p><strong>Instructions:</strong></p>
                            <ul>
                                <li>Présentez votre billet à l'entrée de l'événement</li>
                                <li>Vous pouvez le présenter sur votre téléphone ou imprimé</li>
                                <li>Le code QR du billet sera scanné à l'entrée</li>
                            </ul>
                            <br/>
                            <p>Si vous avez des questions, n'hésitez pas à nous contacter.</p>
                            <p>À bientôt sur CarthageGG! 🎯</p>
                        </div>
                        <div class="footer">
                            <p>© 2026 CarthageGG - Tous droits réservés</p>
                        </div>
                    </div>
                </body>
                </html>
                """, recipientName, reservationId, eventTitle);

        return sendEmailWithPDF(recipientEmail, subject, body, pdfFile);
    }
}