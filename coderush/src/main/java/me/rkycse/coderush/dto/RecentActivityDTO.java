package me.rkycse.coderush.dto;

import java.util.Map;

public class RecentActivityDTO {
    private String username;
    private Map<String, Map<String, Integer>> activity;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Map<String, Integer>> getActivity() {
        return activity;
    }

    public void setActivity(Map<String, Map<String, Integer>> activity) {
        this.activity = activity;
    }
}
