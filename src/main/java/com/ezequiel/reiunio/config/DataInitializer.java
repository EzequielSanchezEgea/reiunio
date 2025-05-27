@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService, GameService gameService) {
        return args -> {
            log.error("=== DATAINITIALIZER EJECUTÁNDOSE ===");
            log.error("Argumentos: {}", Arrays.toString(args));
            log.error("UserService disponible: {}", userService != null);
            log.error("GameService disponible: {}", gameService != null);
            
            try {
                List<User> existingUsers = userService.findAll();
                log.error("Usuarios existentes: {}", existingUsers.size());
                
                if (existingUsers.isEmpty()) {
                    log.error("=== INICIANDO CREACIÓN DE DATOS ===");
                    initializeUsers(userService);
                    initializeGames(gameService);
                    log.error("=== DATOS CREADOS EXITOSAMENTE ===");
                } else {
                    log.error("=== DATOS YA EXISTEN, SALTANDO INICIALIZACIÓN ===");
                    existingUsers.forEach(user -> log.error("Usuario existente: {}", user.getUsername()));
                }
            } catch (Exception e) {
                log.error("=== ERROR EN DATAINITIALIZER ===", e);
                throw e;
            }
        };
    }

    private void initializeUsers(UserService userService) {
        log.error("=== CREANDO USUARIOS ===");
        // ... resto del código igual ...
    }

    private void initializeGames(GameService gameService) {
        log.error("=== CREANDO JUEGOS ===");
        // ... resto del código igual ...
    }
}
