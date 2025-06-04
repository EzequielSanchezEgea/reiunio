package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that represents summary information about a game session.
 * Useful for lightweight API responses or UI components that do not need full entity details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameSessionInfo {

    /**
     * ID of the game session.
     */
    private Long sessionId;

    /**
     * Title of the session.
     */
    private String sessionTitle;

    /**
     * Start date of the session.
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * Start time of the session.
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    /**
     * End date of the session.
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * End time of the session.
     */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    /**
     * Full name of the session creator.
     */
    private String creatorName;

    /**
     * Status of the session (e.g., SCHEDULED, CANCELLED).
     */
    private String status;

    /**
     * Indicates whether the session spans more than one day.
     */
    private boolean isMultiDay;

    /**
     * Returns a formatted string representing the date range of the session.
     * If multi-day, returns "startDate - endDate"; otherwise just the startDate.
     *
     * @return formatted date range
     */
    public String getFormattedDateRange() {
        if (isMultiDay()) {
            return String.format("%s - %s", startDate, endDate);
        } else {
            return startDate.toString();
        }
    }

    /**
     * Returns a formatted string representing the time range of the session.
     * If both start and end times are available, returns "startTime - endTime".
     * If only start time is set, returns "From startTime".
     * Otherwise, returns a default message.
     *
     * @return formatted time range
     */
    public String getFormattedTimeRange() {
        if (startTime != null && endTime != null) {
            return String.format("%s - %s", startTime, endTime);
        } else if (startTime != null) {
            return String.format("From %s", startTime);
        } else {
            return "Time not specified";
        }
    }
}
