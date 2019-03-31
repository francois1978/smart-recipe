package dataloader.entity;


@lombok.Getter
@lombok.Setter
public class TagEntity implements Entity {

    private Long id;

    private String name;


    @Override
    public String toString() {
        return "TagEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
