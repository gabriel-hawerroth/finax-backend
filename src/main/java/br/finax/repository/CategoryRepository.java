package br.finax.repository;

import br.finax.models.Category;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = """
            SELECT
                ctg
            FROM
                Category ctg
            WHERE
                ctg.userId = :userId
                AND ctg.active = true
            ORDER BY
                ctg.id
            """)
    List<Category> findByUser(long userId);

    List<Category> findByIdIn(@NonNull List<Long> id);
}
