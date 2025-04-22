package com.ezequiel.reiunio.entity;

import java.time.LocalDateTime;

import com.ezequiel.reiunio.enums.TipoAccion;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "historial_cambios")
public class HistorialCambios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @Column(name = "fecha_hora_cambio")
    private LocalDateTime fechaHoraCambio;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_accion")
    private TipoAccion tipoAccion;

    @NotBlank
    @Column(name = "entidad_afectada")
    private String entidadAfectada;

    @Column(name = "entidad_id")
    private Long entidadId;

    @NotBlank
    @Column(length = 500)
    private String descripcion;

    // Constructor vacío necesario para JPA
    public HistorialCambios() {
    }

    // Constructor con campos básicos
    public HistorialCambios(Usuario usuario, TipoAccion tipoAccion, String entidadAfectada,
                          Long entidadId, String descripcion) {
        this.usuario = usuario;
        this.tipoAccion = tipoAccion;
        this.entidadAfectada = entidadAfectada;
        this.entidadId = entidadId;
        this.descripcion = descripcion;
        this.fechaHoraCambio = LocalDateTime.now();
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

    public LocalDateTime getFechaHoraCambio() {
        return fechaHoraCambio;
    }

    public void setFechaHoraCambio(LocalDateTime fechaHoraCambio) {
        this.fechaHoraCambio = fechaHoraCambio;
    }

    public TipoAccion getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(TipoAccion tipoAccion) {
        this.tipoAccion = tipoAccion;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    public void setEntidadAfectada(String entidadAfectada) {
        this.entidadAfectada = entidadAfectada;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "HistorialCambios [id=" + id + ", usuario=" + usuario.getUsername() + ", fechaHoraCambio="
                + fechaHoraCambio + ", tipoAccion=" + tipoAccion + ", entidadAfectada=" + entidadAfectada
                + ", entidadId=" + entidadId + "]";
    }
}