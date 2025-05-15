package com.ezequiel.reiunio.entity;

import java.io.Serializable;
import java.util.Objects;

public class GameSessionPlayerId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long gameSession;
    private Long user;
    
    public GameSessionPlayerId() {
    }
    
    public GameSessionPlayerId(Long gameSession, Long user) {
        this.gameSession = gameSession;
        this.user = user;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSessionPlayerId that = (GameSessionPlayerId) o;
        return Objects.equals(gameSession, that.gameSession) && Objects.equals(user, that.user);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(gameSession, user);
    }
    
    // Getters y setters
    public Long getGameSession() {
        return gameSession;
    }
    
    public void setGameSession(Long gameSession) {
        this.gameSession = gameSession;
    }
    
    public Long getUser() {
        return user;
    }
    
    public void setUser(Long user) {
        this.user = user;
    }
}