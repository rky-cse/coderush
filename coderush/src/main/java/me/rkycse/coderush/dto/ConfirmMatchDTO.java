package me.rkycse.coderush.dto;

public class ConfirmMatchDTO {
    private String pendingMatchId;
    private Long userId;

    public ConfirmMatchDTO() {}

    public ConfirmMatchDTO(String pendingMatchId, Long userId) {
        this.pendingMatchId = pendingMatchId;
        this.userId = userId;
    }

    public String getPendingMatchId() { return pendingMatchId; }
    public void setPendingMatchId(String pendingMatchId) { this.pendingMatchId = pendingMatchId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
