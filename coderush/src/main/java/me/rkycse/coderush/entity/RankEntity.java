package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "ranks")
@Getter
@Setter
public class RankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rank_id", nullable = false, unique = true)
    private Long id;

    private Long tournamentId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "score", nullable = false)
    private long score;

}