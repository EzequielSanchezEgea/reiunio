package com.ezequiel.reiunio.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ezequiel.reiunio.entity.Juego;
import com.ezequiel.reiunio.entity.Partida;
import com.ezequiel.reiunio.entity.Prestamo;
import com.ezequiel.reiunio.entity.Usuario;
import com.ezequiel.reiunio.enums.EstadoJuego;
import com.ezequiel.reiunio.enums.EstadoPartida;
import com.ezequiel.reiunio.enums.Rol;
import com.ezequiel.reiunio.repository.JuegoRepository;
import com.ezequiel.reiunio.repository.PartidaRepository;
import com.ezequiel.reiunio.repository.PrestamoRepository;
import com.ezequiel.reiunio.repository.UsuarioRepository;

/**
 * Esta clase inicializa datos de prueba para la aplicación.
 * Solo se ejecutará cuando la aplicación se inicie con el perfil "dev".
 */
@Component
//@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final JuegoRepository juegoRepository;
    private final PrestamoRepository prestamoRepository;
    private final PartidaRepository partidaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UsuarioRepository usuarioRepository, JuegoRepository juegoRepository,
                          PrestamoRepository prestamoRepository, PartidaRepository partidaRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.juegoRepository = juegoRepository;
        this.prestamoRepository = prestamoRepository;
        this.partidaRepository = partidaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Solo inicializar datos si no hay usuarios en la base de datos
        if (usuarioRepository.count() == 0) {
            initUsers();
            initGames();
            initLoans();
            initParties();
        }
    }

    private void initUsers() {
        System.out.println("Iniciando datos de usuarios...");

        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setEmail("admin@reiunio.com");
        admin.setNombre("Administrador");
        admin.setApellidos("Sistema");
        admin.setRol(Rol.ADMIN);
        admin.setFechaRegistro(LocalDate.now());

        Usuario usuario1 = new Usuario();
        usuario1.setUsername("usuario1");
        usuario1.setPassword(passwordEncoder.encode("password"));
        usuario1.setEmail("usuario1@example.com");
        usuario1.setNombre("Usuario");
        usuario1.setApellidos("Uno");
        usuario1.setRol(Rol.USUARIO_BASICO);
        usuario1.setFechaRegistro(LocalDate.now());

        Usuario usuario2 = new Usuario();
        usuario2.setUsername("usuario2");
        usuario2.setPassword(passwordEncoder.encode("password"));
        usuario2.setEmail("usuario2@example.com");
        usuario2.setNombre("Usuario");
        usuario2.setApellidos("Dos");
        usuario2.setRol(Rol.USUARIO_EXTENDIDO);
        usuario2.setFechaRegistro(LocalDate.now());

        usuarioRepository.saveAll(Arrays.asList(admin, usuario1, usuario2));
    }

    private void initGames() {
        System.out.println("Iniciando datos de juegos...");

        Juego juego1 = new Juego();
        juego1.setNombre("Catan");
        juego1.setDescripcion("Juego de estrategia y comercio ambientado en una isla");
        juego1.setMinJugadores(3);
        juego1.setMaxJugadores(4);
        juego1.setDuracionMinutos(120);
        juego1.setCategoria("Estrategia");
        juego1.setDisponible(true);
        juego1.setEstado(EstadoJuego.BUENO);
        juego1.setFechaAdquisicion(LocalDate.now().minusMonths(2));

        Juego juego2 = new Juego();
        juego2.setNombre("Carcassonne");
        juego2.setDescripcion("Juego de colocación de losetas para construir ciudades y caminos");
        juego2.setMinJugadores(2);
        juego2.setMaxJugadores(5);
        juego2.setDuracionMinutos(45);
        juego2.setCategoria("Estrategia");
        juego2.setDisponible(true);
        juego2.setEstado(EstadoJuego.NUEVO);
        juego2.setFechaAdquisicion(LocalDate.now().minusMonths(1));

        Juego juego3 = new Juego();
        juego3.setNombre("Dixit");
        juego3.setDescripcion("Juego de cartas con ilustraciones para adivinar historias");
        juego3.setMinJugadores(3);
        juego3.setMaxJugadores(6);
        juego3.setDuracionMinutos(30);
        juego3.setCategoria("Cartas");
        juego3.setDisponible(true);
        juego3.setEstado(EstadoJuego.BUENO);
        juego3.setFechaAdquisicion(LocalDate.now().minusMonths(3));

        juegoRepository.saveAll(Arrays.asList(juego1, juego2, juego3));
    }

    private void initLoans() {
        System.out.println("Iniciando datos de préstamos...");

        // Recuperar usuarios y juegos
        Usuario usuario1 = usuarioRepository.findByUsername("usuario1").orElseThrow();
        Usuario usuario2 = usuarioRepository.findByUsername("usuario2").orElseThrow();
        
        Juego catan = juegoRepository.findByNombreContainingIgnoreCase("Catan").get(0);
        Juego carcassonne = juegoRepository.findByNombreContainingIgnoreCase("Carcassonne").get(0);

        // Crear préstamos
        Prestamo prestamo1 = new Prestamo();
        prestamo1.setUsuario(usuario1);
        prestamo1.setJuego(catan);
        prestamo1.setFechaPrestamo(LocalDate.now().minusDays(5));
        prestamo1.setFechaDevolucionEstimada(LocalDate.now().plusDays(2));
        prestamo1.setEstado(com.ezequiel.reiunio.enums.EstadoPrestamo.ACTIVO);
        
        // Marcar el juego como no disponible
        catan.setDisponible(false);
        juegoRepository.save(catan);

        prestamoRepository.save(prestamo1);
    }

    private void initParties() {
        System.out.println("Iniciando datos de partidas...");

        // Recuperar usuarios y juegos
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        Usuario usuario1 = usuarioRepository.findByUsername("usuario1").orElseThrow();
        
        Juego dixit = juegoRepository.findByNombreContainingIgnoreCase("Dixit").get(0);
        Juego carcassonne = juegoRepository.findByNombreContainingIgnoreCase("Carcassonne").get(0);

        // Crear partidas
        Partida partida1 = new Partida();
        partida1.setCreador(admin);
        partida1.setJuego(dixit);
        partida1.setTitulo("Tarde de Dixit");
        partida1.setDescripcion("Partida para principiantes");
        partida1.setFecha(LocalDate.now().plusDays(1));
        partida1.setHoraInicio(LocalTime.of(17, 0));
        partida1.setHoraFin(LocalTime.of(19, 0));
        partida1.setMaxJugadores(6);
        partida1.setEstado(EstadoPartida.PROGRAMADA);

        Partida partida2 = new Partida();
        partida2.setCreador(usuario1);
        partida2.setJuego(carcassonne);
        partida2.setTitulo("Campeonato de Carcassonne");
        partida2.setDescripcion("Torneo oficial");
        partida2.setFecha(LocalDate.now().plusDays(7));
        partida2.setHoraInicio(LocalTime.of(10, 0));
        partida2.setHoraFin(LocalTime.of(14, 0));
        partida2.setMaxJugadores(5);
        partida2.setEstado(EstadoPartida.PROGRAMADA);

        partidaRepository.saveAll(Arrays.asList(partida1, partida2));
    }
}
