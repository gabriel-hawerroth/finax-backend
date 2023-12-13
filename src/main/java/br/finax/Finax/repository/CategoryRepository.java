package br.finax.finax.repository;

import br.finax.finax.models.Category;
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
                user_id = :userId
            ORDER BY
                id
            """, nativeQuery = true
    )
    List<Category> findByUser(Long userId);
}
