package me.rkycse.coderush.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "one_to_one_games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class OneToOneGameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tournamentId;

    @Column(name = "player1", nullable = false)
    private String player1;

    @Column(name = "player2", nullable = false)
    private String player2;

    @Column(name = "time_control", nullable = false)
    private Long timeControl;
}