package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "rank_lists")
public class RankListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_list_id", nullable = false, unique = true)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "rank_list_id")
    private List<RankEntity> rankListOfTournament;

    // Getters and Setters
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

    public List<RankEntity> getRankListOfTournament() {
        return rankListOfTournament;
    }

    public void setRankListOfTournament(List<RankEntity> rankListOfTournament) {
        this.rankListOfTournament = rankListOfTournament;
    }
}