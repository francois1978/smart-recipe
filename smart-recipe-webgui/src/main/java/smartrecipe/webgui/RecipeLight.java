package smartrecipe.webgui;

@lombok.Getter
@lombok.Setter
public class RecipeLight {

    private Long id;
    private String name;
    private String description;

    public RecipeLight() {
    }

    public RecipeLight(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
