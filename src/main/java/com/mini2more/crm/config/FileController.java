/** Copyright © 2026 Mini2More. All Rights Reserved. Product: Mini2More CRM **/
package com.mini2more.crm.config;

import com.mini2more.crm.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload/{category}")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @PathVariable String category,
            @RequestParam("file") MultipartFile file) {
        String filePath = fileStorageService.storeFile(file, category);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", filePath));
    }

    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("path") String filePath) {
        Resource resource = fileStorageService.loadFile(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam("path") String filePath) {
        fileStorageService.deleteFile(filePath);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }
}
