package com.devesh.TestingApplication.controllers;

import com.devesh.TestingApplication.TestContainerConfiguration;
import com.devesh.TestingApplication.dto.EmployeeDto;
import com.devesh.TestingApplication.entities.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureWebTestClient(timeout = "10000")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainerConfiguration.class)
public class AbstractIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    Employee testEmployee =Employee.builder()
            .id(1L)
            .email("dev@gmail.com")
            .name("Dev")
            .salary(100L)
            .build();
    EmployeeDto testEmployeeDto=EmployeeDto.builder()
            .id(1L)
            .email("dev@gmail.com")
            .name("Dev")
            .salary(100L)
            .build();
}
