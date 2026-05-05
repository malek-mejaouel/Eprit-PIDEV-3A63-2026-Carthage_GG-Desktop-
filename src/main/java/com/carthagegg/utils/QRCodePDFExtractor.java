package com.carthagegg.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utility class to extract and decode QR codes from PDF files
 */
public class QRCodePDFExtractor {

    /**
     * Extracts QR code data from a PDF file
     * @param pdfFile The PDF file to process
     * @return The decoded QR code data (reservation ID)
     */
    public static String extractQRCodeFromPDF(File pdfFile) throws Exception {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            
            // Render the first page as an image
            BufferedImage image = renderer.renderImageWithDPI(0, 150); // 150 DPI for good quality
            
            // Decode QR code from the image
            return decodeQRCode(image);
        }
    }

    /**
     * Decodes a QR code from a BufferedImage
     * @param image The image containing the QR code
     * @return The decoded QR code data
     */
    public static String decodeQRCode(BufferedImage image) throws Exception {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(image)));
        
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }

    /**
     * Extracts QR code from an image file
     * @param imageFile The image file containing the QR code
     * @return The decoded QR code data
     */
    public static String extractQRCodeFromImage(File imageFile) throws Exception {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Failed to load image: " + imageFile.getAbsolutePath());
        }
        return decodeQRCode(image);
    }
}

