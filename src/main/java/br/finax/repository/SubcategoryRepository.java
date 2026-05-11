package br.finax.repository;

import br.finax.models.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    List<Subcategory> findAllByCategoryId(long categoryId);

    List<Subcategory> findAllByCategoryIdIn(List<Long> categoryIds);

    @Modifying
    @Query("UPDATE Subcategory s SET s.active = false WHERE s.categoryId = :categoryId")
    void deactivateAllByCategoryId(long categoryId);
}

