package br.finax.repository;

import br.finax.models.Category;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = """
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
    @Query(value = """
            INSERT INTO CATEGORY (name, color, icon, type, user_id, essential)
            SELECT name, color, icon, type, :userId, essential
            FROM Category c
            WHERE c.user_id = 0 AND c.id <> 1
            """, nativeQuery = true)
    void insertNewUserCategories(long userId);

    List<Category> findByIdIn(@NonNull List<Long> id);
}
