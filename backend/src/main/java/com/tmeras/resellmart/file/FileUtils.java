package com.tmeras.resellmart.file;

import com.tmeras.resellmart.exception.FileException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static byte[] readFileFromPath(String filePath) {
        if (filePath.isBlank())
            return null;
        try {
            Path path = new File(filePath).toPath();
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new FileException("No file could be read from path: \"" + filePath + "\"");
        }
    }
}