package com.acme.hr.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void notFoundExceptionMapsToNotFoundProblem() {
        ProblemDetail problem = handler.handleNotFound(new ResourceNotFoundException("Employee 9 was not found."));

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Employee 9 was not found.", problem.getDetail());
    }

    @Test
    void illegalArgumentMapsToBadRequestProblem() {
        ProblemDetail problem = handler.handleBadRequest(new IllegalArgumentException("Invalid request."));

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Invalid request.", problem.getDetail());
    }
}
