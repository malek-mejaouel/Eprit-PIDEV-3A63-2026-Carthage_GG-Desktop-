package com.carthagegg.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

/**
 * Sends emails with attachments using SMTP
 * Supports sending PDF tickets to users
 */
public class EmailService {

    // Configuration SMTP - Change these to your email provider settings
    private static final String SMTP_HOST = "smtp.gmail.com"; // Gmail SMTP
    private static final int SMTP_PORT = 587;
    private static final String SENDER_EMAIL = "anasbouzid250@gmail.com"; // Your email
    private static final String SENDER_PASSWORD = "phwnkuctnnejxkst"; // Gmail app password
    private static final String SENDER_NAME = "CarthageGG";

    /**
     * Sends an email with PDF attachment
     * @param recipientEmail The recipient's email address
     * @param subject Email subject
     * @param body Email body text
     * @param pdfFile The PDF file to attach
     * @return true if email was sent successfully
     */
    public static boolean sendEmailWithPDF(String recipientEmail, String subject, String body, File pdfFile) {
        try {
            // Setup mail properties
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Create multipart message for text + attachment
            MimeMultipart multipart = new MimeMultipart();

            // Text part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body, "utf-8", "html");
            multipart.addBodyPart(textPart);

            // Attachment part
            if (pdfFile != null && pdfFile.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(pdfFile);
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);

            // Send message
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
     * Sends a simple text email without attachment
     * @param recipientEmail The recipient's email address
     * @param subject Email subject
     * @param body Email body text
     * @return true if email was sent successfully
     */
    public static boolean sendEmail(String recipientEmail, String subject, String body) {
        return sendEmailWithPDF(recipientEmail, subject, body, null);
    }

    /**
     * Sends a reservation confirmation email with PDF ticket
     * @param recipientEmail User's email
     * @param recipientName User's name
     * @param reservationId Reservation ID
     * @param eventTitle Event title
     * @param pdfFile PDF ticket file
     * @return true if email was sent successfully
     */
    public static boolean sendReservationConfirmation(String recipientEmail, String recipientName, 
                                                       int reservationId, String eventTitle, File pdfFile) {
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

