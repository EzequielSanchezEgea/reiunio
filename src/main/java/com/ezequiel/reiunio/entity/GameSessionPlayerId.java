package com.ezequiel.reiunio.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionPlayerId implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long gameSession;
    private Long user;
    

    
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
    
 
}