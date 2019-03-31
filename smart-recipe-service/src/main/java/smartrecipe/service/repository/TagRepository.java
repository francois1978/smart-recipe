package smartrecipe.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smartrecipe.service.entity.TagEntity;

public interface TagRepository extends JpaRepository<TagEntity, Long> {
}
