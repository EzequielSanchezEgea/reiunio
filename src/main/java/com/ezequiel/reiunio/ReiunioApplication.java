package com.ezequiel.reiunio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ReiunioApplication implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ReiunioApplication.class);
    
    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(ReiunioApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Aplicación iniciada correctamente");
        logger.info("Puerto configurado: {}", env.getProperty("server.port"));
        logger.info("URL de base de datos: {}", env.getProperty("spring.datasource.url"));
    }
}
