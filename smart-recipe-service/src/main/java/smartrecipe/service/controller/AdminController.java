package smartrecipe.service.controller;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import smartrecipe.service.entity.AdminEntity;
import smartrecipe.service.repository.AdminRepository;

import javax.annotation.Resource;

@Slf4j
@RestController
public class AdminController {

    @Resource
    private AdminRepository adminRepository;

    @GetMapping("/healthcheck2")
    @ApiOperation("Health check")
    String healthCheck2() {
        return "OK";
    }


    @RequestMapping(value = "/admin/addconfig", method = RequestMethod.POST)
    @ApiOperation("Create a new key / value admin config in table admin.")
    AdminEntity newPlateType(@RequestBody AdminEntity adminEntity) {

        log.info("Creating admin enity entry " + adminEntity);
        AdminEntity entityResult = adminRepository.save(adminEntity);
        log.info("Admin entity created " + adminEntity);
        return entityResult;

    }

}
