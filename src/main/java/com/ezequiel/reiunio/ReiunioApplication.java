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
        // Mostrar información de la base de datos (ocultando contraseña)
        String dbUrl = env.getProperty("spring.datasource.url");
        String dbUser = env.getProperty("spring.datasource.username");
        
        logger.info("Aplicación iniciada correctamente");
        logger.info("URL de base de datos: {}", dbUrl);
        logger.info("Usuario de base de datos: {}", dbUser);
        logger.info("Puerto del servidor: {}", env.getProperty("server.port", "8080"));
        
        // Detectar si estamos en Railway
        boolean isRailway = System.getenv("RAILWAY_SERVICE_NAME") != null;
        if (isRailway) {
            logger.info("Ejecutando en Railway");
        } else {
            logger.info("Ejecutando en entorno local");
        }
    }
}