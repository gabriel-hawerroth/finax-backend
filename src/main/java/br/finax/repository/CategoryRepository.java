package br.finax.repository;

import br.finax.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
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
    List<Category> findByUser(long userId);

    @Modifying
    @Transactional
    @Query(
        value =
            """
            INSERT INTO CATEGORY (name, color, icon, type, user_id)
            SELECT name, color, icon, type, :userId
            FROM Category c
            WHERE c.user_id = 0 AND c.id <> 21
            """, nativeQuery = true)
    void insertNewUserCategories(long userId);
}
