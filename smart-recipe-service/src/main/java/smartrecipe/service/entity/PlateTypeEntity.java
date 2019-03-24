package smartrecipe.service.entity;


import javax.persistence.*;

@Entity
@Table(name = "plate_type")
@lombok.Getter
@lombok.Setter
public class PlateTypeEntity implements SimpleEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Override
    public String toString() {
        return "PlateTypeEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
