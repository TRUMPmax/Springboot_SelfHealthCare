package com.example.selfhealthcare.controller;

import com.example.selfhealthcare.dto.ImportResultResponse;
import com.example.selfhealthcare.service.DocumentImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
public class DocumentImportController {

    private final DocumentImportService documentImportService;

    public DocumentImportController(DocumentImportService documentImportService) {
        this.documentImportService = documentImportService;
    }

    @PostMapping(value = "/health-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImportResultResponse importHealthDocument(@RequestPart("file") MultipartFile file) {
        return documentImportService.importDocument(file);
    }
}
