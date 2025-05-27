package com.ezequiel.reiunio.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameSessionInfo {
    
    private Long sessionId;
    private String sessionTitle;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    
    private String creatorName;
    private String status;
    private boolean isMultiDay;
    
    public String getFormattedDateRange() {
        if (isMultiDay()) {
            return String.format("%s - %s", startDate, endDate);
        } else {
            return startDate.toString();
        }
    }
    
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