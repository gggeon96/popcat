package dev.gunn96.popcat;

import org.springframework.boot.SpringApplication;

public class TestPopcatApplication {

    public static void main(String[] args) {
        SpringApplication.from(PopcatApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
