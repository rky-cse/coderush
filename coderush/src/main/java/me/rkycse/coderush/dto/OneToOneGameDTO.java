package me.rkycse.coderush.dto;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class OneToOneGameDTO {

    private Long tournamentId;

    private String player1;

    private String player2;

    private Long timeControl;
}
