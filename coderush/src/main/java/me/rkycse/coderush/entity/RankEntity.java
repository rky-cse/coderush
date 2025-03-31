package me.rkycse.coderush.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ranks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id", "user_name"})
})
public class RankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "score", nullable = false)
    private long score;

    @Column(name="penalty", nullable = false)
    private Long penalty=0L;

    @Column(name="rating", nullable = false)
    private Long rating=0L;

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "RankEntity{" +
                "id=" + id +
                ", tournamentId=" + tournamentId +
                ", userName='" + userName + '\'' +
                ", score=" + score +
                ", penalty=" + penalty +
                ", rating=" + rating +
                '}';
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public Long getPenalty() {
        return penalty;
    }

    public void setPenalty(Long penalty) {
        this.penalty = penalty;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}
