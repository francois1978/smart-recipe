package smartrecipe.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartrecipe.service.entity.RecipeBinaryEntity;

public interface RecipeBinaryRepository extends JpaRepository<RecipeBinaryEntity, Long> {

    RecipeBinaryEntity findByBinaryDescriptionChecksum(String checksum);

}
