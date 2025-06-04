package com.ezequiel.reiunio.enums;

/**
 * Enum representing the status of a game session.
 */
public enum GameSessionStatus {

    /**
     * The session is scheduled but has not started yet.
     */
    SCHEDULED,

    /**
     * The session is currently in progress.
     */
    IN_PROGRESS,

    /**
     * The session has finished successfully.
     */
    FINISHED,

    /**
     * The session was cancelled before completion.
     */
    CANCELLED
}
