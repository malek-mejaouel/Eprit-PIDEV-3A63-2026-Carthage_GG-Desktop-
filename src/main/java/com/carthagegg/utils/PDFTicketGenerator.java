package com.carthagegg.utils;

import com.carthagegg.models.Event;
import com.carthagegg.models.Location;
import com.carthagegg.models.Reservation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Generates PDF tickets for reservations with QR Codes
 * Creates beautifully formatted PDF files for printing or digital distribution
 */
public class PDFTicketGenerator {

    private static final String COMPANY_NAME = "CarthageGG";
    private static final float MARGIN = 40;
    private static final float LINE_HEIGHT = 15;
    private static final float SECTION_SPACING = 10;

    /**
     * Generates a PDF reservation ticket with QR Code
     * @param reservation The reservation to generate
     * @param event The associated event
     * @param location The event location
     * @return File object pointing to the generated PDF ticket
     */
    public static File generateReservationTicket(Reservation reservation, Event event, Location location) throws Exception {
        String fileName = "Ticket_" + reservation.getId() + "_" + System.currentTimeMillis() + ".pdf";
        File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float yPosition = pageHeight - MARGIN;

                PDFont titleFont = PDType1Font.HELVETICA_BOLD;
                PDFont headerFont = PDType1Font.HELVETICA_BOLD;
                PDFont textFont = PDType1Font.HELVETICA;
                PDFont smallFont = PDType1Font.HELVETICA_OBLIQUE;

                // Header
                yPosition = drawSection(contentStream, pageWidth, yPosition, 
                    "=  CarthageGG - BILLET DE RÉSERVATION  =", 
                    titleFont, 14, true);
                yPosition -= SECTION_SPACING;

                // Ticket Number and Status
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "N° Ticket: " + reservation.getId(), textFont, 11);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Statut: " + formatStatus(reservation.getStatus().toString()), textFont, 11);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Date de réservation: " + reservation.getReservationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 
                    textFont, 11);
                yPosition -= SECTION_SPACING;

                // Reservation Information Section
                yPosition = drawSection(contentStream, pageWidth, yPosition, 
                    "INFORMATIONS DE LA RÉSERVATION", headerFont, 12, false);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Nom: " + reservation.getName(), textFont, 10);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Nombre de places: " + reservation.getSeats(), textFont, 10);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Prix total: " + reservation.getPrice() + " TND", textFont, 10);
                yPosition -= SECTION_SPACING;

                // Event Details Section
                yPosition = drawSection(contentStream, pageWidth, yPosition, 
                    "DÉTAILS DE L'ÉVÉNEMENT", headerFont, 12, false);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Événement: " + event.getTitle(), textFont, 10);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Date de début: " + (event.getStartAt() != null ? 
                        event.getStartAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"), 
                    textFont, 10);
                if (event.getEndAt() != null) {
                    yPosition = drawLine(contentStream, pageWidth, yPosition, 
                        "Date de fin: " + event.getEndAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 
                        textFont, 10);
                }
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Description: " + (event.getDescription() != null ? event.getDescription() : "N/A"), 
                    textFont, 10);
                yPosition -= SECTION_SPACING;

                // Location Details Section
                yPosition = drawSection(contentStream, pageWidth, yPosition, 
                    "LIEU DE L'ÉVÉNEMENT", headerFont, 12, false);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Lieu: " + location.getName(), textFont, 10);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Adresse: " + location.getAddress(), textFont, 10);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Capacité: " + location.getCapacity() + " personnes", textFont, 10);
                if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                    yPosition = drawLine(contentStream, pageWidth, yPosition, 
                        "Coordonnées: " + location.getLatitude() + ", " + location.getLongitude(), 
                        textFont, 10);
                }
                yPosition -= SECTION_SPACING;

                // Footer
                yPosition = drawSection(contentStream, pageWidth, yPosition, 
                    "Merci de votre confiance!", headerFont, 11, true);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Conservez ce billet pour l'accès à l'événement.", smallFont, 9);
                yPosition = drawLine(contentStream, pageWidth, yPosition, 
                    "Généré le: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), 
                    smallFont, 9);
                
                // QR Code Section (bottom center)
                BufferedImage qrImage = QRCodeGenerator.generateQRCode(reservation.getId());
                File qrFile = convertBufferedImageToFile(qrImage, reservation.getId());
                PDImageXObject pdImage = PDImageXObject.createFromFile(qrFile.getAbsolutePath(), document);
                
                // Draw QR Code at bottom center
                float qrWidth = 120;
                float qrHeight = 120;
                float qrXPosition = (pageWidth - qrWidth) / 2; // Center horizontally
                float qrYPosition = yPosition - qrHeight - 20; // Below the footer text
                contentStream.drawImage(pdImage, qrXPosition, qrYPosition, qrWidth, qrHeight);
                
                // Cleanup temp QR file
                qrFile.delete();
            }

            document.save(tempFile);
        }

        return tempFile;
    }

    /**
     * Converts BufferedImage to a temporary file
     */
    private static File convertBufferedImageToFile(BufferedImage image, int reservationId) throws IOException {
        String fileName = "QRCode_temp_" + reservationId + ".png";
        File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
        
        if (!javax.imageio.ImageIO.write(image, "png", tempFile)) {
            throw new IOException("Failed to write QR code image");
        }
        
        return tempFile;
    }

    /**
     * Draws a line of text at the current Y position
     */
    private static float drawLine(PDPageContentStream contentStream, float pageWidth, float yPosition, 
                                  String text, PDFont font, int fontSize) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        return yPosition - LINE_HEIGHT;
    }

    /**
     * Draws a section header
     */
    private static float drawSection(PDPageContentStream contentStream, float pageWidth, float yPosition, 
                                     String text, PDFont font, int fontSize, boolean centered) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        
        float xPosition = centered ? (pageWidth - MARGIN - (text.length() * 3.5f)) / 2 : MARGIN;
        contentStream.newLineAtOffset(xPosition, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        return yPosition - LINE_HEIGHT;
    }

    /**
     * Formats the status for display
     */
    private static String formatStatus(String status) {
        return switch (status) {
            case "WAITING" -> "EN ATTENTE [EN COURS]";
            case "CONFIRMED" -> "CONFIRMÉE [OK]";
            case "CANCELLED" -> "ANNULÉE [REFUSEE]";
            default -> status;
        };
    }
}









