package dev.gunn96.popcat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PopcatApplicationTests {

    @Test
    void contextLoads() {
    }

}
