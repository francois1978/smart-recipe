package smartrecipe.webgui.dto;

import java.util.Objects;

@lombok.Getter
@lombok.Setter
public class RecipeLight {

    private Long id;
    private String name;
    private String description;
    private String tagAsString = "";


    public RecipeLight() {
    }

    public RecipeLight(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecipeLight that = (RecipeLight) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
