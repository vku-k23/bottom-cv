package com.cnpm.bottomcv.controller;

import com.cnpm.bottomcv.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search API", description = "Skeleton endpoints for search and suggestions")
@RestController
@RequestMapping(value = "/api/v1/front/search", produces = { MediaType.APPLICATION_JSON_VALUE })
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/jobs")
    public ResponseEntity<Void> searchJobs(@RequestParam(required = false) String q) {
        searchService.searchJobs(q);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/suggestions")
    public ResponseEntity<Void> getSuggestions(@RequestParam String q) {
        searchService.getSuggestions(q);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}