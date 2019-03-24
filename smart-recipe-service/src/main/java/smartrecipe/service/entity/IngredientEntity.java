package smartrecipe.service.entity;


import javax.persistence.*;
import javax.persistence.Entity;

@Entity
@Table(name = "ingredient")
@lombok.Getter
@lombok.Setter
public class IngredientEntity implements SimpleEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Override
    public String toString() {
        return "IngredientEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
