package me.rkycse.coderush.dto;

/**
 * DTO representing health check response.
 */
public class HealthStatusDTO {
    private String status;
    private long timestamp;

    public HealthStatusDTO() {}

    public HealthStatusDTO(String status, long timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
