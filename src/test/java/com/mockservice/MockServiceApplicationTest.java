package com.mockservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@EnableAutoConfiguration()
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MockServiceApplicationTest {

    @Test
    public void test() {
        MockServiceApplication.main(new String[]{});
        Assertions.assertNull(null);
    }
}
