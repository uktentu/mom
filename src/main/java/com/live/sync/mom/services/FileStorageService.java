package com.live.sync.mom.services;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileStorageService {
    private final String storageDir = "storage/";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(storageDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    public String storeFile(MultipartFile file, String subDir) {
        try {
            String dirPath = storageDir + subDir;
            Files.createDirectories(Paths.get(dirPath));

            String fileName = FilenameUtils.getName(file.getOriginalFilename());
            String filePath = dirPath + "/" + fileName;

            file.transferTo(new File(filePath));
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
