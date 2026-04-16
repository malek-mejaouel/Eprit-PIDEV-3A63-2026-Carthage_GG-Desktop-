package com.carthagegg.utils;

import com.carthagegg.models.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.math.BigDecimal;

public class CartManager {
    private static final ObservableMap<Product, Integer> cartItems = FXCollections.observableHashMap();

    public static void addProduct(Product product) {
        cartItems.put(product, cartItems.getOrDefault(product, 0) + 1);
    }

    public static void removeProduct(Product product) {
        cartItems.remove(product);
    }

    public static void updateQuantity(Product product, int quantity) {
        if (quantity <= 0) {
            removeProduct(product);
        } else {
            cartItems.put(product, quantity);
        }
    }

    public static ObservableMap<Product, Integer> getCartItems() {
        return cartItems;
    }

    public static int getTotalItems() {
        return cartItems.values().stream().mapToInt(Integer::intValue).sum();
    }

    public static BigDecimal getTotalPrice() {
        return cartItems.entrySet().stream()
                .map(entry -> entry.getKey().getPrice().multiply(new BigDecimal(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static void clear() {
        cartItems.clear();
    }
}
