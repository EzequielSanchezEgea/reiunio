package com.ezequiel.reiunio.entity;

import java.io.Serializable;
import java.util.Objects;

public class JugadorPartidaId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long partida;
    private Long usuario;
    
    public JugadorPartidaId() {
    }
    
    public JugadorPartidaId(Long partida, Long usuario) {
        this.partida = partida;
        this.usuario = usuario;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JugadorPartidaId that = (JugadorPartidaId) o;
        return Objects.equals(partida, that.partida) && Objects.equals(usuario, that.usuario);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(partida, usuario);
    }
    
    // Getters y setters
    public Long getPartida() {
        return partida;
    }
    
    public void setPartida(Long partida) {
        this.partida = partida;
    }
    
    public Long getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Long usuario) {
        this.usuario = usuario;
    }
}