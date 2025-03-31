package com.cnpm.bottomcv.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Job API", description = "The API of job")
@RestController
@RequestMapping(value = "/api/job", produces = {MediaType.APPLICATION_JSON_VALUE})
@RequiredArgsConstructor
public class JobController {
}
