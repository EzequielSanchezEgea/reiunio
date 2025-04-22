package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.ezequiel.reiunio.enums.EstadoPrestamo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "juego_id", nullable = false)
    private Juego juego;

    @NotNull
    @Column(name = "fecha_prestamo")
    private LocalDate fechaPrestamo;

    @NotNull
    @Column(name = "fecha_devolucion_estimada")
    private LocalDate fechaDevolucionEstimada;

    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    private EstadoPrestamo estado;

    // Constructor vacío necesario para JPA
    public Prestamo() {
    }

    // Constructor con campos básicos
    public Prestamo(Usuario usuario, Juego juego, LocalDate fechaPrestamo, LocalDate fechaDevolucionEstimada) {
        this.usuario = usuario;
        this.juego = juego;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
        this.estado = EstadoPrestamo.ACTIVO;
    }

    // Método para registrar la devolución
    public void registrarDevolucion(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.estado = EstadoPrestamo.DEVUELTO;
        
        // Marcar el juego como disponible nuevamente
        if (this.juego != null) {
            this.juego.setDisponible(true);
        }
    }

    // Método para calcular los días de retraso (si los hay)
    public int calcularDiasRetraso() {
        if (fechaDevolucionReal == null) {
            // Si aún no se ha devuelto, calculamos en base a la fecha actual
            LocalDate hoy = LocalDate.now();
            if (hoy.isAfter(fechaDevolucionEstimada)) {
                return (int) ChronoUnit.DAYS.between(fechaDevolucionEstimada, hoy);
            }
        } else if (fechaDevolucionReal.isAfter(fechaDevolucionEstimada)) {
            // Si ya se devolvió con retraso
            return (int) ChronoUnit.DAYS.between(fechaDevolucionEstimada, fechaDevolucionReal);
        }
        return 0; // No hay retraso
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Juego getJuego() {
        return juego;
    }

    public void setJuego(Juego juego) {
        this.juego = juego;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucionEstimada() {
        return fechaDevolucionEstimada;
    }

    public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) {
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
    }

    public LocalDate getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDate fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Prestamo [id=" + id + ", usuario=" + usuario.getUsername() + ", juego=" + juego.getNombre()
                + ", fechaPrestamo=" + fechaPrestamo + ", fechaDevolucionEstimada=" + fechaDevolucionEstimada
                + ", fechaDevolucionReal=" + fechaDevolucionReal + ", estado=" + estado + "]";
    }
}