package com.cts.cts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:15:///cts_db",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CtsApplicationTests {

    @Test
    void contextLoads() {
    }
}