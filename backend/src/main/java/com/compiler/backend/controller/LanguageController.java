package com.compiler.backend.controller;

import com.compiler.backend.dto.LanguageDTO;
import com.compiler.backend.service.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LanguageController {
    private final LanguageService languageService;
    @GetMapping("/languages")
    public LanguageDTO getLanguages() {
        return languageService.getLanguages();
    }

}
