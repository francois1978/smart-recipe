package smartrecipe.service.entity;

import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;


@Entity
@Table(name = "recipe")
@lombok.Getter
@lombok.Setter
@Indexed
public class RecipeEntity {

    //extends AuditModel

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "auto_description")
    @Field
    private String autoDescription;

    private String comment;

    private String name;

    @Column(name = "binary_description")
    private byte[] binaryDescription;


    public RecipeEntity(Long id, String name, String description) {
        this.id = id;
        this.description = description;
        this.name = name;
    }

    public RecipeEntity() {
    }

    public RecipeEntity(Long id, String name, String description, byte[] binaryDescription) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.binaryDescription = binaryDescription;
    }

    @Override
    public String toString() {
        return "RecipeEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + (description != null ? description.substring(0, Math.min(80, description.length())) : "No description") + '\'' +

                ", binary description length='" + (binaryDescription != null ? binaryDescription.length : "No binary description") + '\'' +
                '}';
    }
}
