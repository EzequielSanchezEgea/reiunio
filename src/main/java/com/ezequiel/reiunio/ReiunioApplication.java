package com.ezequiel.reiunio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class ReiunioApplication implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ReiunioApplication.class);
    
    @Autowired
    private Environment env;

    public static void main(String[] args) {
        // Forzar configuración de puerto antes de iniciar Spring Boot
        int port = 8081; // Puerto diferente del predeterminado 8080
        try {
            String envPort = System.getenv("PORT");
            if (envPort != null && !envPort.isEmpty()) {
                port = Integer.parseInt(envPort);
            }
        } catch (Exception e) {
            System.err.println("Error al leer variable de entorno PORT: " + e.getMessage());
        }
        
        // Forzar configuración del puerto a través de propiedades del sistema
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("server.address", "0.0.0.0");
        
        // Registro para diagnóstico
        System.out.println("========================================================");
        System.out.println("INICIANDO APLICACIÓN EN PUERTO: " + port);
        System.out.println("VARIABLES DE ENTORNO:");
        System.out.println("PORT: " + System.getenv("PORT"));
        System.out.println("RAILWAY_SERVICE_NAME: " + System.getenv("RAILWAY_SERVICE_NAME"));
        System.out.println("========================================================");
        
        SpringApplication.run(ReiunioApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Aplicación iniciada correctamente");
        logger.info("URL de base de datos: {}", env.getProperty("spring.datasource.url"));
        logger.info("Puerto del servidor: {}", env.getProperty("server.port", "No configurado"));
        logger.info("Dirección del servidor: {}", env.getProperty("server.address", "No configurada"));
        
        // Detectar si estamos en Railway
        boolean isRailway = System.getenv("RAILWAY_SERVICE_NAME") != null;
        if (isRailway) {
            logger.info("Ejecutando en Railway");
        } else {
            logger.info("Ejecutando en entorno local");
        }
    }
    
    // Forzar configuración de puerto a nivel de servidor web
    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            // Leer puerto de variable de entorno o usar 8081 como respaldo
            int port = 8081;
            try {
                String envPort = System.getenv("PORT");
                if (envPort != null && !envPort.isEmpty()) {
                    port = Integer.parseInt(envPort);
                } else if (System.getProperty("server.port") != null) {
                    port = Integer.parseInt(System.getProperty("server.port"));
                }
            } catch (Exception e) {
                logger.error("Error al configurar puerto: " + e.getMessage());
            }
            
            // Configurar puerto y dirección explícitamente
            factory.setPort(port);
            
            // Configurar dirección - ESTE ES EL MÉTODO QUE CAUSA EL ERROR
            // Necesitamos manejar la excepción UnknownHostException
            try {
                factory.setAddress(InetAddress.getByName("0.0.0.0"));
                logger.info("Configurado servidor web para usar puerto {} y dirección 0.0.0.0", port);
            } catch (UnknownHostException e) {
                logger.error("Error al configurar dirección del servidor: " + e.getMessage());
                // Intentamos un enfoque alternativo
                try {
                    factory.setAddress(InetAddress.getLocalHost());
                    logger.info("Configurado servidor web para usar puerto {} y dirección localhost", port);
                } catch (UnknownHostException ex) {
                    logger.error("No se pudo configurar dirección del servidor: " + ex.getMessage());
                }
            }
        };
    }
}
