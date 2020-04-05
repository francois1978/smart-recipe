package smartrecipe.service.controller;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import smartrecipe.service.entity.AdminEntity;
import smartrecipe.service.repository.AdminRepository;

import javax.annotation.Resource;

@Slf4j
@RestController
public class AdminController {

    @Resource
    private AdminRepository adminRepository;

    @RequestMapping(value = "/admin/addconfig", method = RequestMethod.POST)
    @ApiOperation("Create a new key / value admin config in table admin.")
    AdminEntity newPlateType(@RequestBody AdminEntity adminEntity) {

        log.info("Creating admin enity entry " + adminEntity);
        AdminEntity entityResult = adminRepository.save(adminEntity);
        log.info("Admin entity created " + adminEntity);
        return entityResult;

    }

}
