package smartrecipe.webgui;

import java.util.HashSet;
import java.util.Set;

@lombok.Getter
@lombok.Setter
public class RecipeEntity implements Entity {

    //extends AuditModel

    private Long id;

    private Long version;

    private String description;

    private String autoDescription;

    private String comment;

    private String name;

    private byte[] binaryDescription;

    private String binaryDescriptionChecksum;

    private boolean nameModifiedManual;

    private RecipeBinaryEntity recipeBinaryEntity;

    private Set<TagEntity> tags = new HashSet<>();


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
                ", binary description=" + (recipeBinaryEntity != null ? recipeBinaryEntity.toString() : "No binary description") +
                ", binary description length='" + (binaryDescription != null ? binaryDescription.length : "No binary description") + '\'' +
                '}';
    }
}
