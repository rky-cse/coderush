package me.rkycse.coderush.dto;

public class CancelMatchDTO {
    private String pendingMatchId;
    private Long userId;

    public CancelMatchDTO() {}

    public CancelMatchDTO(String pendingMatchId, Long userId) {
        this.pendingMatchId = pendingMatchId;
        this.userId = userId;
    }

    public String getPendingMatchId() { return pendingMatchId; }
    public void setPendingMatchId(String pendingMatchId) { this.pendingMatchId = pendingMatchId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
