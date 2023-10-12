package net.englab.contextsearcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ContextSearcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContextSearcherApplication.class, args);
    }

}
