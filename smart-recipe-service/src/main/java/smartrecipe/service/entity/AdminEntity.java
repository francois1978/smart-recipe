package smartrecipe.service.entity;


import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "admin")
@lombok.Getter
@lombok.Setter
@ToString
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String key;

    private String value;
}
