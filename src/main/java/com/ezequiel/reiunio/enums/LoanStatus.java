package com.ezequiel.reiunio.enums;

/**
 * Enum representing the status of a loaned item.
 */
public enum LoanStatus {

    /**
     * The item is currently on loan and has not yet been returned.
     */
    ACTIVE,

    /**
     * The item has been returned.
     */
    RETURNED,

    /**
     * The item is overdue and has not been returned on time.
     */
    LATE
}
