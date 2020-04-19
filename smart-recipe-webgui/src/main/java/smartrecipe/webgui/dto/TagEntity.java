package smartrecipe.webgui.dto;


import java.util.Objects;

@lombok.Getter
@lombok.Setter
public class TagEntity implements Entity {

    private Long id;

    private String name;


    @Override
    public String toString() {
        return name.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagEntity tagEntity = (TagEntity) o;
        return Objects.equals(id, tagEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
