package com.carthagegg.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileStorage {

    public static String saveNewsImage(File source) throws IOException {
        if (source == null) return null;
        String name = source.getName();
        String safeName = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = System.currentTimeMillis() + "_" + safeName;

        Path dir = Path.of(System.getProperty("user.home"), "CarthageGG", "uploads", "news");
        Files.createDirectories(dir);

        Path target = dir.resolve(fileName);
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    public static String saveAvatar(File source) throws IOException {
        if (source == null) return null;
        String name = source.getName();
        String safeName = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = "avatar_" + System.currentTimeMillis() + "_" + safeName;

        Path dir = Path.of(System.getProperty("user.home"), "CarthageGG", "uploads", "avatars");
        Files.createDirectories(dir);

        Path target = dir.resolve(fileName);
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }
}
