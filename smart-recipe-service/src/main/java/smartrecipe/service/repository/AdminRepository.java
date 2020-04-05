package smartrecipe.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartrecipe.service.entity.AdminEntity;

import java.util.List;

public interface AdminRepository extends JpaRepository<AdminEntity, Long> {

    List<AdminEntity> findByKeyIgnoreCase(String name);

}
