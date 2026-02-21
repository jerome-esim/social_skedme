package com.socialskedme.scheduler.controller;

import com.socialskedme.scheduler.dto.UploadResponse;
import com.socialskedme.scheduler.service.MediaUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaUploadService mediaUploadService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        UploadResponse response = mediaUploadService.upload(file);
        return ResponseEntity.ok(response);
    }
}
