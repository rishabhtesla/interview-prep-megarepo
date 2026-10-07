package com.interviewprep.catalog;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiErrors extends ResponseEntityExceptionHandler {
    // Spring maps validation and ResponseStatusException to RFC 9457 ProblemDetail.
}
