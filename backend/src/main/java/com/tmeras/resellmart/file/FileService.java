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

    @Value("${application.file.upload.user-images-path}")
    private String userImageUploadPath;

    @Value("${application.file.upload.order-item-images-path}")
    private String orderItemImageUploadPath;

    public byte[] readFileFromPath(String filePath) {
        if (filePath.isBlank())
            return null;
        try {
            // Handle OS differences
            if (File.separatorChar == '/')
                filePath = filePath.replace('\\', '/');
            else if (File.separatorChar == '\\')
                filePath = filePath.replace('/', '\\');

            Path path = new File(filePath).toPath();
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new FileException("No file could be read from path: \"" + filePath + "\"");
        }
    }

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
        return saveFile(image, productImageUploadPath, fileUploadSubPath, fileNo);
    }

    public String saveUserImage(@NonNull MultipartFile image, @NonNull Integer userId) throws IOException {
        final String fileUploadSubPath = "users" + File.separator + userId;
        return saveFile(image, userImageUploadPath, fileUploadSubPath, 1);
    }

    public String saveOrderItemImage(byte[] file, @NonNull String fileName, @NonNull Integer productId) throws IOException {
        final String fileUploadSubPath = "order-items" + File.separator + productId;
        return saveFile(file, fileName, orderItemImageUploadPath, fileUploadSubPath, 1);
    }

    public String saveFile(
            @NonNull MultipartFile file, @NonNull String fileUploadPath, @NonNull String fileUploadSubPath, @NonNull Integer fileId
    ) throws IOException{
        final String finalUploadPath = fileUploadPath + File.separator + fileUploadSubPath;
        Path targetFolder = Path.of(finalUploadPath);
        if (!Files.exists(targetFolder))
            Files.createDirectories(targetFolder);

        final String fileExtension = getFileExtension(file.getOriginalFilename());
        String targetFilePath = finalUploadPath + File.separator + System.currentTimeMillis() + "F" + fileId + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        Files.write(targetPath, file.getBytes());
        System.out.println("Created file: " + targetFilePath);
        return targetFilePath;
    }

    public String saveFile(
            byte[] file, @NonNull String fileName, @NonNull String fileUploadPath,
            @NonNull String fileUploadSubPath, @NonNull Integer fileId
    ) throws IOException {
        final String finalUploadPath = fileUploadPath + File.separator + fileUploadSubPath;
        Path targetFolder = Path.of(finalUploadPath);
        if (!Files.exists(targetFolder))
            Files.createDirectories(targetFolder);

        final String fileExtension = getFileExtension(fileName);
        String targetFilePath = finalUploadPath + File.separator + System.currentTimeMillis() + "F" + fileId + "." + fileExtension;
        Path targetPath = Paths.get(targetFilePath);
        Files.write(targetPath, file);
        System.out.println("Created file: " + targetFilePath);
        return targetFilePath;
    }

    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty())
            return "";

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1)
            return "";

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (Files.deleteIfExists(path))
            System.out.println("Deleted file: " + filePath);
    }
}