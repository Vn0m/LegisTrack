package com.legistrack.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// turn on again to test embeddings
@SpringBootApplication(exclude = {
    org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration.class
})
@EnableCaching
public class LegisTrackApplication {
    public static void main(String[] args) {
        SpringApplication.run(LegisTrackApplication.class, args);
    }
}


