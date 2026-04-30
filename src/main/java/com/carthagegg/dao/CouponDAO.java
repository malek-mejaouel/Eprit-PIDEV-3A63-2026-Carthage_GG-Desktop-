package com.carthagegg.dao;

import com.carthagegg.models.Coupon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CouponDAO {
    private static final String FILE_PATH = "coupons.json";
    private final Gson gson;

    public CouponDAO() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new com.google.gson.JsonDeserializer<LocalDate>() {
                    @Override
                    public LocalDate deserialize(com.google.gson.JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                        return LocalDate.parse(json.getAsString());
                    }
                })
                .registerTypeAdapter(LocalDate.class, new com.google.gson.JsonSerializer<LocalDate>() {
                    @Override
                    public com.google.gson.JsonElement serialize(LocalDate src, Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                        return new com.google.gson.JsonPrimitive(src.toString());
                    }
                })
                .setPrettyPrinting()
                .create();
    }

    public List<Coupon> findAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            // Create some default coupons if file doesn't exist
            List<Coupon> defaults = new ArrayList<>();
            defaults.add(new Coupon("WELCOME10", 10.0, LocalDate.now().plusMonths(1)));
            defaults.add(new Coupon("BESTAPP20", 20.0, LocalDate.now().plusMonths(6)));
            saveAll(defaults);
            return defaults;
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Coupon>>() {}.getType();
            List<Coupon> list = gson.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Optional<Coupon> findByCode(String code) {
        return findAll().stream()
                .filter(c -> c.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    public void save(Coupon coupon) {
        List<Coupon> list = findAll();
        list.removeIf(c -> c.getCode().equalsIgnoreCase(coupon.getCode()));
        list.add(coupon);
        saveAll(list);
    }

    public void saveAll(List<Coupon> list) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
