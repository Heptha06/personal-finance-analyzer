package com.heptha.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinanceAnalyzerApplication {
    public static void main(String[] args) {
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword == null || dbPassword.isBlank()) {
            throw new IllegalStateException(
                    "Required environment variable DB_PASSWORD is not set. "
                            + "Set it to the local MySQL password before starting the backend.");
        }

        SpringApplication.run(FinanceAnalyzerApplication.class, args);
    }
}
