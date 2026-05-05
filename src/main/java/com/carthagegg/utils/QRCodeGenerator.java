package com.carthagegg.utils;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Generates QR Codes for reservation tickets using QRGen (free, Apache 2.0 license)
 * Each reservation gets a unique QR code containing the reservation ID
 */
public class QRCodeGenerator {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    private static final String FORMAT = "png";

    /**
     * Generates a QR code image for a reservation
     * @param reservationId The unique reservation ID
     * @return BufferedImage containing the QR code
     */
    public static BufferedImage generateQRCode(int reservationId) throws Exception {
        return generateQRCode(String.valueOf(reservationId));
    }

    /**
     * Generates a QR code image for a reservation
     * @param reservationData The data to encode in the QR code
     * @return BufferedImage containing the QR code
     */
    public static BufferedImage generateQRCode(String reservationData) throws Exception {
        return ImageIO.read(new ByteArrayInputStream(((ByteArrayOutputStream) QRCode.from(reservationData).to(ImageType.PNG).withSize(WIDTH, HEIGHT).stream()).toByteArray()));
    }

    /**
     * Generates a QR code file for a reservation
     * @param reservationId The unique reservation ID
     * @return File object pointing to the generated QR code image
     */
    public static File generateQRCodeFile(int reservationId) throws Exception {
        String fileName = "QRCode_" + reservationId + "_" + System.currentTimeMillis() + ".png";
        File qrCodeFile = new File(System.getProperty("java.io.tmpdir"), fileName);

        BufferedImage qrImage = generateQRCode(reservationId);
        
        if (!ImageIO.write(qrImage, FORMAT, qrCodeFile)) {
            throw new IOException("Failed to write QR code image to file");
        }

        return qrCodeFile;
    }

    /**
     * Generates a QR code with detailed reservation information
     * @param reservationId The reservation ID
     * @param eventTitle The event title
     * @param reservationName The name of the reservation
     * @return BufferedImage containing the QR code
     */
    public static BufferedImage generateDetailedQRCode(int reservationId, String eventTitle, String reservationName) throws Exception {
        String qrData = String.format("RESERVATION|ID:%d|EVENT:%s|NAME:%s|TIME:%d", 
            reservationId, eventTitle, reservationName, System.currentTimeMillis());
        return generateQRCode(qrData);
    }

    /**
     * Generates a simple QR code containing just the reservation ID
     * @param reservationId The reservation ID
     * @return File object pointing to the generated QR code image
     */
    public static File generateSimpleQRCode(int reservationId) throws Exception {
        return generateQRCodeFile(reservationId);
    }
}
