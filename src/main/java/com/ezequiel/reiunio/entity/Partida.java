package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.ezequiel.reiunio.enums.EstadoPartida;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "partidas")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @ManyToOne
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @NotBlank
    @Column(length = 100)
    private String titulo;

    @NotNull
    private LocalDate fecha;

    @NotNull
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @NotNull
    @Min(1)
    @Column(name = "max_jugadores")
    private Integer maxJugadores;

    @Column(length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private EstadoPartida estado;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JugadorPartida> jugadores = new ArrayList<>();

    // Constructor vacío necesario para JPA
    public Partida() {
    }

    // Constructor con campos básicos
    public Partida(Usuario creador, Juego juego, String titulo, LocalDate fecha, LocalTime horaInicio, 
                  LocalTime horaFin, Integer maxJugadores, String descripcion) {
        this.creador = creador;
        this.juego = juego;
        this.titulo = titulo;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.maxJugadores = maxJugadores;
        this.descripcion = descripcion;
        this.estado = EstadoPartida.PROGRAMADA;
    }

    // Método para agregar un jugador a la partida
    public boolean agregarJugador(Usuario usuario) {
        // Verificar si el jugador ya está en la partida
        boolean yaRegistrado = jugadores.stream()
                .anyMatch(jp -> jp.getUsuario().getId().equals(usuario.getId()));
        
        if (yaRegistrado) {
            return false;
        }
        
        // Verificar si hay espacio disponible
        if (getNumeroJugadoresConfirmados() >= maxJugadores) {
            return false;
        }
        
        // Agregar el jugador
        JugadorPartida jugadorPartida = new JugadorPartida();
        jugadorPartida.setUsuario(usuario);
        jugadorPartida.setPartida(this);
        jugadorPartida.setFechaUnion(LocalDate.now());
        jugadorPartida.setConfirmado(false);
        
        jugadores.add(jugadorPartida);
        return true;
    }

    // Método para eliminar un jugador de la partida
    public boolean eliminarJugador(Usuario usuario) {
        return jugadores.removeIf(jugadorPartida -> 
            jugadorPartida.getUsuario().getId().equals(usuario.getId()));
    }

    // Método para obtener el número de jugadores confirmados
    public int getNumeroJugadoresConfirmados() {
        return (int) jugadores.stream()
                .filter(JugadorPartida::isConfirmado)
                .count();
    }

    // Método para verificar si la partida está completa
    public boolean estaCompleta() {
        return getNumeroJugadoresConfirmados() >= maxJugadores;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getCreador() {
        return creador;
    }

    public void setCreador(Usuario creador) {
        this.creador = creador;
    }

    public Juego getJuego() {
        return juego;
    }

    public void setJuego(Juego juego) {
        this.juego = juego;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Integer getMaxJugadores() {
        return maxJugadores;
    }

    public void setMaxJugadores(Integer maxJugadores) {
        this.maxJugadores = maxJugadores;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoPartida getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartida estado) {
        this.estado = estado;
    }

    public List<JugadorPartida> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<JugadorPartida> jugadores) {
        this.jugadores = jugadores;
    }

    @Override
    public String toString() {
        return "Partida [id=" + id + ", creador=" + creador.getUsername() + ", juego=" + juego.getNombre()
                + ", titulo=" + titulo + ", fecha=" + fecha + ", horaInicio=" + horaInicio + ", horaFin=" + horaFin
                + ", maxJugadores=" + maxJugadores + ", estado=" + estado + "]";
    }
}