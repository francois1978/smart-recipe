package smartrecipe.service.entity;


import javax.persistence.*;

@Entity
@Table(name = "tag")
@lombok.Getter
@lombok.Setter
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

}
