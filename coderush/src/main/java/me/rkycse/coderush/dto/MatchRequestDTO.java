package me.rkycse.coderush.dto;

import java.time.LocalDateTime;
public class MatchRequestDTO {
    private Long userId;
    private Long rating;
    private Long requestTime;
    private Long timeControl;

    // Constructors
    public MatchRequestDTO() {}

    public MatchRequestDTO(Long userId, Long rating, Long requestTime, Long timeControl) {
        this.userId = userId;
        this.rating = rating;
        this.requestTime = requestTime;
        this.timeControl = timeControl;
    }

    // Getters/Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRating() { return rating; }
    public void setRating(Long rating) { this.rating = rating; }

    public Long getRequestTime() { return requestTime; }
    public void setRequestTime(Long requestTime) { this.requestTime = requestTime; }

    public Long getTimeControl() { return timeControl; }
    public void setTimeControl(Long timeControl) { this.timeControl = timeControl; }
}