package smartrecipe.service.helper.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import smartrecipe.service.dto.AdminEntityKeysEnum;
import smartrecipe.service.entity.AdminEntity;
import smartrecipe.service.helper.AdminService;
import smartrecipe.service.repository.AdminRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    private static final Integer GOOGLE_API_MAX_CALL_PER_MONTH = 500;

    @Override
    public void checkAndIncrementGoogleAPICall(AdminEntityKeysEnum adminEntityKeysEnum) throws Exception {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");

        //get now data
        Date now = new Date();
        cal.setTime(now);
        int monthOfNow = cal.get(Calendar.MONTH);
        int yearOfNow = cal.get(Calendar.YEAR);

        //load admin entity
        List<AdminEntity> adminEntities = adminRepository.findByKeyIgnoreCase(adminEntityKeysEnum.getKey());
        //create if not exits
        if (CollectionUtils.isEmpty(adminEntities)) {
            AdminEntity adminEntity = new AdminEntity();
            adminEntity.setKey(adminEntityKeysEnum.getKey());
            adminEntity.setValue(dateFormat.format(now) + "-1");
            adminRepository.save(adminEntity);
            log.info("Admin entity created " + adminEntity);
            return;
        }

        //do check if admin entity already exists
        AdminEntity adminEntity = adminEntities.get(0);

        //get data
        String[] valueSplitted = adminEntity.getValue().split("-");
        Date dateOfCounter = dateFormat.parse(valueSplitted[0].trim());
        Integer counter = Integer.parseInt(valueSplitted[1].trim());

        cal.setTime(dateOfCounter);
        int monthOfCounter = cal.get(Calendar.MONTH);
        int yearOfCounter = cal.get(Calendar.YEAR);


        //check and update values
        if (yearOfNow > yearOfCounter || monthOfNow > monthOfCounter) {
            adminEntity.setValue(dateFormat.format(now) + "-1");
        } else if (counter > GOOGLE_API_MAX_CALL_PER_MONTH) {
            throw new Exception("Max count of calls for Google vision API reached, max count per month is " + GOOGLE_API_MAX_CALL_PER_MONTH);
        } else {
            log.info("Check OK on google API call " + adminEntityKeysEnum.getKey() + " Current new counter: " + counter);
            counter++;
            adminEntity.setValue(dateFormat.format(now) + "-" + counter);
        }

        adminRepository.save(adminEntity);
        log.info("Admin entity updated " + adminEntity);
    }

}
