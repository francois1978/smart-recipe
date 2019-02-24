package dataloader;

import javax.persistence.*;

@Entity
@Table(name = "recipe")
public class RecipeEntity {

    //extends AuditModel

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

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

    public byte[] getBinaryDescription() {
        return binaryDescription;
    }

    public void setBinaryDescription(byte[] binaryDescription) {
        this.binaryDescription = binaryDescription;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
