package me.rkycse.coderush.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import me.rkycse.coderush.converter.JsonMapConverter;

@Entity
@Table(name = "recent_activity")
public class RecentActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    // Using a TEXT column to store JSON and the JsonMapConverter for conversion
    @Column(columnDefinition = "TEXT")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Map<String, Integer>> json;

    // Default constructor
    public RecentActivityEntity() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Map<String, Integer>> getJson() {
        return json;
    }

    public void setJson(Map<String, Map<String, Integer>> json) {
        this.json = json;
    }
}
