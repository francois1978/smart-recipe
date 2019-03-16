package dataloader.entity;

@lombok.Getter
@lombok.Setter
public class RecipeEntity implements Entity {

    //extends AuditModel

    private Long id;

    private String description;

    private String autoDescription;

    private String comment;

    private String name;

    private byte[] binaryDescription;

    private String binaryDescriptionChecksum;

    private boolean nameModifiedManual;

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
