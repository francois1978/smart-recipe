package smartrecipe.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartrecipe.service.entity.RecipeBinaryEntity;

import java.util.List;

public interface RecipeBinaryRepository extends JpaRepository<RecipeBinaryEntity, Long> {

    List<RecipeBinaryEntity> findByBinaryDescriptionChecksum(String checksum);

}
