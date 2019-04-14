package smartrecipe.service.dto;

@lombok.Getter
@lombok.Setter
public class RecipeBinaryLight {

    private Long id;
    private byte[] binaryDescription;
    private String name;

    public RecipeBinaryLight(Long id, byte[] binaryDescription) {
        this.id = id;
        this.binaryDescription = binaryDescription;
    }

    public RecipeBinaryLight(Long id, byte[] binaryDescription, String name) {
        this.id = id;
        this.binaryDescription = binaryDescription;
        this.name = name;
    }

    public RecipeBinaryLight() {
    }
}
