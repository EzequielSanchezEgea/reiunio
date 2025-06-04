package com.ezequiel.reiunio.entity;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite primary key class for the GameSessionPlayer entity.
 * Combines the gameSession ID and user ID to uniquely identify each participant in a session.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionPlayerId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID of the associated GameSession.
     */
    private Long gameSession;

    /**
     * ID of the participating User.
     */
    private Long user;

    /**
     * Equality check based on both gameSession and user IDs.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameSessionPlayerId that = (GameSessionPlayerId) o;
        return Objects.equals(gameSession, that.gameSession) &&
               Objects.equals(user, that.user);
    }

    /**
     * Hash code generation based on both fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(gameSession, user);
    }
}
