// JugadorPartida.java
package com.ezequiel.reiunio.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "jugadores_partida")
@IdClass(JugadorPartidaId.class)
public class JugadorPartida {

    @Id
    @ManyToOne
    @JoinColumn(name = "partida_id")
    private Partida partida;

    @Id
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotNull
    @Column(name = "fecha_union")
    private LocalDate fechaUnion;

    private Boolean confirmado = false;

    // Constructor vacío necesario para JPA
    public JugadorPartida() {
    }

    // Constructor con campos básicos
    public JugadorPartida(Partida partida, Usuario usuario, LocalDate fechaUnion, Boolean confirmado) {
        this.partida = partida;
        this.usuario = usuario;
        this.fechaUnion = fechaUnion;
        this.confirmado = confirmado;
    }

    // Getters y setters
    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFechaUnion() {
        return fechaUnion;
    }

    public void setFechaUnion(LocalDate fechaUnion) {
        this.fechaUnion = fechaUnion;
    }

    public Boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(Boolean confirmado) {
        this.confirmado = confirmado;
    }

    @Override
    public String toString() {
        return "JugadorPartida [partida=" + partida.getId() + ", usuario=" + usuario.getUsername() + ", fechaUnion="
                + fechaUnion + ", confirmado=" + confirmado + "]";
    }
}