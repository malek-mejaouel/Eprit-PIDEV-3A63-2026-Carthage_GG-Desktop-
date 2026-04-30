package com.carthagegg.utils;

import com.carthagegg.models.Product;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionListParams;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.FileInputStream;

public class StripeService {
    private static String STRIPE_SECRET_KEY; 
    
    static {
        try {
            Properties props = new Properties();
            // Try to load from project root first
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                props.load(fis);
                STRIPE_SECRET_KEY = props.getProperty("stripe.api.key");
            }
            
            if (STRIPE_SECRET_KEY == null || STRIPE_SECRET_KEY.isEmpty()) {
                System.err.println("Warning: stripe.api.key not found in config.properties");
            } else {
                Stripe.apiKey = STRIPE_SECRET_KEY;
            }
        } catch (IOException e) {
            System.err.println("Error loading config.properties: " + e.getMessage());
        }
    }

    public static String createCheckoutSession(Map<Product, Integer> items, double discountPercentage) throws Exception {
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://checkout.stripe.com/test/success") 
                .setCancelUrl("https://checkout.stripe.com/test/cancel");

        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();

            // Use effective price (sale price if exists)
            BigDecimal pricePerUnit = product.getEffectivePrice();
            
            // Apply coupon discount if any
            if (discountPercentage > 0) {
                BigDecimal discount = pricePerUnit.multiply(new BigDecimal(discountPercentage))
                        .divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                pricePerUnit = pricePerUnit.subtract(discount);
            }

            long amountInCents = pricePerUnit.multiply(new BigDecimal(100)).longValue();

            builder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) quantity)
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("usd")
                                            .setUnitAmount(amountInCents)
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(product.getName())
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        Session session = Session.create(builder.build());
        return session.getUrl();
    }

    public static void openCheckoutInBrowser(String url) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Session> getRecentSessions() throws Exception {
        SessionListParams params = SessionListParams.builder()
                .setLimit(20L)
                .build();

        return Session.list(params).getData();
    }
}
