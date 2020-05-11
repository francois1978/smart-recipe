package smartrecipe.service.dto;

import lombok.NoArgsConstructor;
import smartrecipe.service.entity.TagEntity;

import java.util.Objects;

@lombok.Getter
@lombok.Setter
@NoArgsConstructor
public class RecipeLight {

    private Long id;
    private String name;
    private String description;
    private TagEntity tagEntity;
    private String tagAsString = "";

    public RecipeLight(Long id, String name, String description, TagEntity tagEntity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tagEntity = tagEntity;
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

    @Override
    public String toString() {
        return "RecipeLight{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", tagEntity=" + tagEntity +
                '}';
    }
}
