package smartrecipe.service.entity;

import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;


@Entity
@Table(name = "recipe_binary")
@lombok.Getter
@lombok.Setter
@Indexed
public class RecipeBinaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "binary_description")
    private byte[] binaryDescription;

    @Column(name = "binary_description_checksum")
    private String binaryDescriptionChecksum;

    public RecipeBinaryEntity() {
    }

    public RecipeBinaryEntity(byte[] binaryDescription, String binaryDescriptionChecksum) {
        this.binaryDescription = binaryDescription;
        this.binaryDescriptionChecksum = binaryDescriptionChecksum;
    }

    @Override
    public String toString() {
        return "RecipeEntity{" +
                "id=" + id +
                ", binary description length='" + (binaryDescription != null ? binaryDescription.length : "No binary description") + '\'' +
                '}';
    }
}
