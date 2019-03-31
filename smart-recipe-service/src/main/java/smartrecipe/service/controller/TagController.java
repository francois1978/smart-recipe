package smartrecipe.service.controller;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.entity.TagEntity;
import smartrecipe.service.repository.TagRepository;

import java.util.List;

@Slf4j
@RestController
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    @RequestMapping(value = "/tag", method = RequestMethod.POST)
    @ApiOperation("Create a new tag.")
    TagEntity createTag(@RequestBody TagEntity tag) {
        TagEntity tagEntity = tagRepository.save(tag);
        log.info("Tag created: " + tagEntity);
        return tag;
    }

    @GetMapping("/tags")
    @ApiOperation("Find all tags.")
    List<TagEntity> all() {
        List allTags = tagRepository.findAll();
        return allTags;
    }

    @DeleteMapping("/tag/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        tagRepository.deleteById(id);
    }


}
