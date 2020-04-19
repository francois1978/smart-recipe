package smartrecipe.webgui.dto;

@lombok.Getter
@lombok.Setter

public class RecipeBinaryEntity {


    private Long id;

   // private RecipeEntity recipeEntity;

    private byte[] binaryDescription;

    private String binaryDescriptionChecksum;

    //private RecipeEntity recipe;

    public RecipeBinaryEntity(byte[] binaryDescription, String binaryDescriptionChecksum) {
        this.binaryDescription = binaryDescription;
        this.binaryDescriptionChecksum = binaryDescriptionChecksum;
    }

    public RecipeBinaryEntity() {
    }

    @Override
    public String toString() {
        return "RecipeEntity{" +
                "id=" + id +
                ", binary description length='" + (binaryDescription != null ? binaryDescription.length : "No binary description") + '\'' +
                '}';
    }
}
