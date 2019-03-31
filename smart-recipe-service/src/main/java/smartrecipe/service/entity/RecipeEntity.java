package smartrecipe.service.entity;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "recipe")
@lombok.Getter
@lombok.Setter
@Indexed
public class RecipeEntity {

    //extends AuditModel

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "auto_description")
    @Field
    private String autoDescription;

    private String comment;

    private String name;

    //@Column(name = "binary_description")
    @Column(name = "tmp")
    private byte[] binaryDescription;

    @Column(name = "binary_description_checksum")
    private String binaryDescriptionChecksum;

    @Column(name = "name_modified_manual")
    private boolean nameModifiedManual;

    @OneToOne(optional = true,
            cascade = CascadeType.ALL
    )
    //@LazyToOne(LazyToOneOption.NO_PROXY)
    @JoinColumn(name = "recipe_binary_id")
    private RecipeBinaryEntity recipeBinaryEntity;


    @ManyToMany(fetch = FetchType.LAZY,
            cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}
    )
    @JoinTable(
            name = "recipe_tag",
            joinColumns = {@JoinColumn(name = "recipe_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")}
    )
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
                '}';
    }
}
