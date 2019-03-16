package smartrecipe.service.entity;


import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@Table(name = "ingredient")
@lombok.Getter
@lombok.Setter
public class IngredientEntity {

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
