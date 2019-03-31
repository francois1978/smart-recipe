package smartrecipe.webgui;

import java.util.Set;

@lombok.Getter
@lombok.Setter
public class RecipeFindParameter {

    private String description;
    private Set<TagEntity> tags;

}
