package br.finax.repository;

import br.finax.models.Category;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = """
            SELECT
                *
            FROM
                category ctg
            WHERE
                ctg.user_id = :userId
                AND ctg.active = true
            ORDER BY
                ctg.id
            """, nativeQuery = true
    )
    List<Category> findByUser(long userId);

    List<Category> findByIdIn(@NonNull List<Long> id);
}
