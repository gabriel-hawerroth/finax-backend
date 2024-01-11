package br.finax.repository;

import br.finax.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(
        value =
            """
            SELECT
                *
            FROM
                category c
            WHERE
                c.user_id = :userId
                AND c.active = true
            ORDER BY
                id
            """, nativeQuery = true
    )
    List<Category> findByUser(Long userId);
}
