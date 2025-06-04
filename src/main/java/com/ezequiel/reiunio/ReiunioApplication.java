package com.ezequiel.reiunio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for the Reiunio Spring Boot application.
 * It initializes the application and logs some environment-specific information.
 */
@SpringBootApplication
public class ReiunioApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ReiunioApplication.class);

    @Autowired
    private Environment env;

    /**
     * Main method that starts the Spring Boot application.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ReiunioApplication.class, args);
    }

    /**
     * Method executed after the application context is loaded and the application has started.
     *
     * @param args Command-line arguments passed to the application
     * @throws Exception if an error occurs during execution
     */
    @Override
    public void run(String... args) throws Exception {
        // Display database information (excluding password)
        String dbUrl = env.getProperty("spring.datasource.url");
        String dbUser = env.getProperty("spring.datasource.username");

        logger.info("Application started successfully");
        logger.info("Database URL: {}", dbUrl);
        logger.info("Database user: {}", dbUser);
        logger.info("Server port: {}", env.getProperty("server.port", "8080"));

        // Detect if running on Railway
        boolean isRailway = System.getenv("RAILWAY_SERVICE_NAME") != null;
        if (isRailway) {
            logger.info("Running on Railway environment");
        } else {
            logger.info("Running in local environment");
        }
    }
}
