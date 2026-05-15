package br.finax.services;

import br.finax.dto.cash_flow.CashFlowCategory;
import br.finax.dto.cash_flow.CashFlowSubcategory;
import br.finax.dto.category.SaveCategoryDTO;
import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Category;
import br.finax.models.Subcategory;
import br.finax.repository.CategoryRepository;
import br.finax.repository.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static br.finax.utils.DefaultCategories.DEFAULT_EXPENSE_CATEGORIES;
import static br.finax.utils.DefaultCategories.DEFAULT_REVENUE_CATEGORIES;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    @Transactional(readOnly = true)
    public Category findById(long id) {
        final Category category = categoryRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        checkPermission(category);

        return category;
    }

    @Transactional(readOnly = true)
    public List<Category> getByUser() {
        return categoryRepository.findAllByUserId(
                getAuthUser().getId()
        );
    }

    @Transactional(readOnly = true)
    public List<Category> findAllActiveByUser() {
        return categoryRepository.findAllActiveByUser(
                getAuthUser().getId()
        );
    }

    @Transactional(readOnly = true)
    public List<CashFlowCategory> getByUserWithSubcategories() {
        final List<Category> activeCategories = findAllActiveByUser();

        final List<Long> categoryIds = activeCategories.stream()
                .map(Category::getId)
                .toList();

        final var subcategoriesByCategoryId = subcategoryRepository.findAllByCategoryIdInAndActiveTrue(categoryIds)
                .stream()
                .collect(Collectors.groupingBy(Subcategory::getCategoryId));

        return activeCategories.stream()
                .map(ctg -> new CashFlowCategory(
                        ctg.getId(),
                        ctg.getName(),
                        ctg.getColor(),
                        ctg.getIcon(),
                        ctg.getType(),
                        subcategoriesByCategoryId
                                .getOrDefault(ctg.getId(), List.of())
                                .stream()
                                .map(sub -> new CashFlowSubcategory(sub.getId(), sub.getName()))
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public Category createNew(SaveCategoryDTO categoryDto) {
        final Category category = categoryDto.toEntity();
        category.setUserId(getAuthUser().getId());
        category.setActive(true);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category edit(long categoryId, SaveCategoryDTO categoryDto) {
        final Category category = categoryDto.toEntity();

        final Category oldCategory = findById(categoryId);

        category.setId(categoryId);
        category.setUserId(oldCategory.getUserId());
        category.setActive(oldCategory.isActive());

        checkPermission(category);

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(long id) {
        final Category category = findById(id);

        checkPermission(category);

        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            category.setActive(false);
            categoryRepository.save(category);
            subcategoryRepository.deactivateAllByCategoryId(id);
        }
    }

    @Transactional(readOnly = true)
    public List<Category> findByIdIn(List<Long> categoryIds) {
        return categoryRepository.findByIdIn(categoryIds);
    }

    @Transactional
    public void insertNewUserCategories(long userId) {
        final var categories = getDefaultCategories();
        categories.forEach(category -> category.setUserId(userId));

        categoryRepository.saveAll(categories);
    }

    private List<Category> getDefaultCategories() {
        final var categories = new java.util.ArrayList<>(DEFAULT_EXPENSE_CATEGORIES);
        categories.addAll(DEFAULT_REVENUE_CATEGORIES);
        return categories;
    }

    private void checkPermission(Category category) {
        if (!category.getUserId().equals(getAuthUser().getId()))
            throw new WithoutPermissionException();
    }
}
