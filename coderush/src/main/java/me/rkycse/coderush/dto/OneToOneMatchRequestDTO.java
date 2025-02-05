package me.rkycse.coderush.dto;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OneToOneMatchRequestDTO {
    String userName;
    Long rating;
    Long timeControl;
    Long timestamp;

}
