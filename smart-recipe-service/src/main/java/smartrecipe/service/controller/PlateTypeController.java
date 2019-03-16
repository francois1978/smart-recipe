package smartrecipe.service.controller;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.entity.IngredientEntity;
import smartrecipe.service.entity.PlateTypeEntity;
import smartrecipe.service.repository.IngredientRepository;
import smartrecipe.service.repository.PlateTypeRepository;

import java.util.List;

@Slf4j
@RestController
public class PlateTypeController {

    @Autowired
    private PlateTypeRepository repository;


    @GetMapping("/platetype")
    @ApiOperation("Find all plate type.")
    List<PlateTypeEntity> all() {
        return repository.findAll();
    }

    @GetMapping("/platetype/{name}")
    @ApiOperation("Find plate type by name")
    List<PlateTypeEntity> findByDescription(@PathVariable("name") String name) {
        List<PlateTypeEntity> list = repository.findByNameContainingIgnoreCase(name);
        log.info("Number of plates loaded: " + list.size());
        return list;
    }

    @RequestMapping(value = "/platetype", method = RequestMethod.POST)
    @ApiOperation("Create a new plate type.")
    PlateTypeEntity newPlateType(@RequestBody PlateTypeEntity plateTypeEntity) {

        PlateTypeEntity entity= repository.save(plateTypeEntity);
        log.info("Plate type created: " + plateTypeEntity.toString());
        return plateTypeEntity;

    }

    @RequestMapping(value = "/platetype/{id}")
    @ApiOperation("Get plate type by id")
    public ResponseEntity<IngredientEntity> getPlateTypeById(@PathVariable("id") Long id) {
        return new ResponseEntity(repository.findById(id), HttpStatus.OK);
    }


}
