package com.company.inspection.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@Slf4j
public class TestController {

    @GetMapping("/log")
    public ResponseEntity<String> testLogging(){
        log.debug("DEBUG level log - Test endpoint called");
        log.info("INFO level log - Processing test request");
        log.warn("WARN level log - This is a warning test");
        log.error("ERROR level log - This is an error test (not real error)");

        return ResponseEntity.ok("Logging test completed! Check logs/application.log");
    }

    @GetMapping("/exception")
    public ResponseEntity<String> testException() {
        try{
            log.info("Testing exception handling");
            throw new RuntimeException("This is a test exception");
        } catch (RuntimeException e) {
            log.error("Caught an exception: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Exception handled successfully!");
        }
    }

}
