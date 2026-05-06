package com.carthagegg.dao;

import com.carthagegg.models.Order;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {
    private static final String FILE_PATH = "orders.json";
    private final Gson gson;

    public OrderDAO() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(com.google.gson.JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                        return LocalDateTime.parse(json.getAsString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonSerializer<LocalDateTime>() {
                    @Override
                    public com.google.gson.JsonElement serialize(LocalDateTime src, Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                        return new com.google.gson.JsonPrimitive(src.toString());
                    }
                })
                .setPrettyPrinting()
                .create();
    }

    public List<Order> findAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Order>>() {}.getType();
            List<Order> list = gson.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<Integer, Integer> getProductSalesCounts() {
        List<Order> orders = findAll();
        Map<Integer, Integer> counts = new HashMap<>();
        for (Order o : orders) {
            counts.put(o.getProductId(), counts.getOrDefault(o.getProductId(), 0) + o.getQuantity());
        }
        return counts;
    }

    public void save(Order o) {
        List<Order> list = findAll();
        int maxId = list.stream().mapToInt(Order::getOrderId).max().orElse(0);
        o.setOrderId(maxId + 1);
        o.setOrderDate(LocalDateTime.now());
        list.add(o);
        saveToFile(list);
    }

    public void updateStatus(int orderId, Order.Status status) {
        List<Order> list = findAll();
        for (Order o : list) {
            if (o.getOrderId() == orderId) {
                o.setStatus(status);
                break;
            }
        }
        saveToFile(list);
    }

    private void saveToFile(List<Order> list) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
