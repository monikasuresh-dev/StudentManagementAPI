package com.monika.studentmanagementapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the application. @SpringBootApplication turns on component
 * scanning (finds the controller/service classes) and auto-configuration
 * (starts the embedded Tomcat server on port 8080).
 */
@SpringBootApplication
public class StudentManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApiApplication.class, args);
    }
}
