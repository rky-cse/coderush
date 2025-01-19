package me.rkycse.coderush.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "ranks")
public class RankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "rank_id")
    private HashSet<UserTestcaseEntity> rankListTestcases;

    @Column(name = "score", nullable = false)
    private long score;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public HashSet<UserTestcaseEntity> getRankListTestcases() {
        return rankListTestcases;
    }

    public void setRankListTestcases(HashSet<UserTestcaseEntity> rankListTestcases) {
        this.rankListTestcases = rankListTestcases;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}