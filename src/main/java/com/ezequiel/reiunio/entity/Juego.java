package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.ezequiel.reiunio.enums.EstadoJuego;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "juegos")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @NotNull
    @Min(1)
    @Column(name = "min_jugadores")
    private Integer minJugadores;

    @NotNull
    @Min(1)
    @Column(name = "max_jugadores")
    private Integer maxJugadores;

    @NotNull
    @Min(1)
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    private String categoria;

    private Boolean disponible = true;

    @Column(name = "fecha_adquisicion")
    private LocalDate fechaAdquisicion;

    @Enumerated(EnumType.STRING)
    private EstadoJuego estado;

    @OneToMany(mappedBy = "juego")
    private List<Prestamo> prestamos = new ArrayList<>();

    @OneToMany(mappedBy = "juego")
    private List<Partida> partidas = new ArrayList<>();

    // Constructor vacío necesario para JPA
    public Juego() {
    }

    // Constructor con campos básicos
    public Juego(String nombre, String descripcion, Integer minJugadores, Integer maxJugadores, 
                Integer duracionMinutos, String categoria, EstadoJuego estado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.minJugadores = minJugadores;
        this.maxJugadores = maxJugadores;
        this.duracionMinutos = duracionMinutos;
        this.categoria = categoria;
        this.estado = estado;
        this.fechaAdquisicion = LocalDate.now();
        this.disponible = true;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getMinJugadores() {
        return minJugadores;
    }

    public void setMinJugadores(Integer minJugadores) {
        this.minJugadores = minJugadores;
    }

    public Integer getMaxJugadores() {
        return maxJugadores;
    }

    public void setMaxJugadores(Integer maxJugadores) {
        this.maxJugadores = maxJugadores;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public LocalDate getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public void setFechaAdquisicion(LocalDate fechaAdquisicion) {
        this.fechaAdquisicion = fechaAdquisicion;
    }

    public EstadoJuego getEstado() {
        return estado;
    }

    public void setEstado(EstadoJuego estado) {
        this.estado = estado;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    public List<Partida> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<Partida> partidas) {
        this.partidas = partidas;
    }

    @Override
    public String toString() {
        return "Juego [id=" + id + ", nombre=" + nombre + ", minJugadores=" + minJugadores + ", maxJugadores="
                + maxJugadores + ", duracionMinutos=" + duracionMinutos + ", categoria=" + categoria + ", disponible="
                + disponible + ", estado=" + estado + "]";
    }
}