package dataloader.dto;

import dataloader.entity.TagEntity;

import java.util.Set;

@lombok.Getter
@lombok.Setter
public class RecipeFindParameter {

    private String description;
    private Set<TagEntity> tags;

}
