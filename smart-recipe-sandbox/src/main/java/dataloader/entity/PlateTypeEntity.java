package dataloader.entity;


@lombok.Getter
@lombok.Setter

public class PlateTypeEntity implements Entity{

    private Long id;

    private String name;

    @Override
    public String toString() {
        return "PlateTypeEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
