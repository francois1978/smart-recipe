package smartrecipe.service.sensor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartrecipe.service.sensor.entity.SensorDataEntity;

import java.util.Date;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorDataEntity, Long> {

    List<SensorDataEntity> findByType(String type);

    List<SensorDataEntity> findAllByTypeAndInputDateBetween(String type, Date startDate, Date endDate);

}
