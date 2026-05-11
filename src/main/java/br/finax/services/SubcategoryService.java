package br.finax.services;

import br.finax.dto.category.SaveSubcategoryDTO;
import br.finax.enums.release.ReleaseType;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Category;
import br.finax.models.Subcategory;
import br.finax.repository.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<Subcategory> getByCategoryId(long categoryId) {
        final Category category = categoryService.findById(categoryId);
        checkCategoryPermission(category);

        return subcategoryRepository.findAllByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public Subcategory findById(long id) {
        final Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        checkPermission(subcategory);

        return subcategory;
    }

    @Transactional
    public Subcategory createNew(SaveSubcategoryDTO dto) {
        final Category category = categoryService.findById(dto.categoryId());
        checkCategoryPermission(category);

        validateEssential(dto.essential(), category);

        final Subcategory subcategory = new Subcategory(dto.name(), dto.essential(), dto.categoryId());
        return subcategoryRepository.save(subcategory);
    }

    @Transactional
    public Subcategory edit(long id, SaveSubcategoryDTO dto) {
        final Subcategory subcategory = findById(id);
        final Category category = categoryService.findById(subcategory.getCategoryId());
        checkCategoryPermission(category);

        validateEssential(dto.essential(), category);

        subcategory.setName(dto.name());
        subcategory.setEssential(dto.essential());

        return subcategoryRepository.save(subcategory);
    }

    @Transactional
    public void deleteById(long id) {
        final Subcategory subcategory = findById(id);
        checkPermission(subcategory);

        try {
            subcategoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            subcategory.setActive(false);
            subcategoryRepository.save(subcategory);
        }
    }

    @Transactional
    public void deactivateAllByCategoryId(long categoryId) {
        subcategoryRepository.deactivateAllByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Subcategory> findAllByCategoryIdIn(List<Long> categoryIds) {
        return subcategoryRepository.findAllByCategoryIdIn(categoryIds);
    }

    private void validateEssential(boolean essential, Category category) {
        if (essential && !ReleaseType.E.name().equals(category.getType())) {
            throw new IllegalArgumentException("Only expense categories can have essential subcategories");
        }
    }

    private void checkPermission(Subcategory subcategory) {
        final Category category = categoryService.findById(subcategory.getCategoryId());
        checkCategoryPermission(category);
    }

    private void checkCategoryPermission(Category category) {
        if (!category.getUserId().equals(getAuthUser().getId()))
            throw new WithoutPermissionException();
    }
}

