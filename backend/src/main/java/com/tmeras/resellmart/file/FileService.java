package com.tmeras.resellmart.file;

import com.tmeras.resellmart.exception.FileException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${application.file.upload.product-images-path}")
    private String productImageUploadPath;

    public List<String> saveProductImages(
            @NonNull List<MultipartFile> images, @NonNull Integer productId
    ) throws IOException {
        List<String> filePaths = new ArrayList<>();

        int fileNo = 1;
        for (MultipartFile image : images)
            filePaths.add(saveProductImage(image, productId, fileNo++));
        return filePaths;
    }

    public String saveProductImage(
            @NonNull MultipartFile image, @NonNull Integer productId, @NonNull Integer fileNo
    ) throws IOException {
        final String fileUploadSubPath = "products" + File.separator + productId;
        return uploadFile(image, productImageUploadPath, fileUploadSubPath, fileNo);
    }

    public String uploadFile(
            @NonNull MultipartFile file, @NonNull String fileUploadPath, @NonNull String fileUploadSubPath, @NonNull Integer fileId
    ) throws IOException{
        final String finalUploadPath = fileUploadPath + File.separator + fileUploadSubPath;
        File targetFolder = new File(finalUploadPath);
        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();
            if (!folderCreated)
                throw new FileException("Failed to create target folder");
        }

        final String fileExtension = getFileExtension(file.getOriginalFilename());
        String targetFilePath = finalUploadPath + File.separator + System.currentTimeMillis() + "F" + fileId + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        Files.write(targetPath, file.getBytes());
        System.out.println("Created file: " + targetFilePath);
        return targetFilePath;
    }

    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return "";

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1)
            return "";

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        Files.deleteIfExists(path);
        System.out.println("Deleted file: " + filePath);
    }
}