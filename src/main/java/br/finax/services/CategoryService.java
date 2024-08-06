package br.finax.services;

import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Category;
import br.finax.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.finax.utils.DefaultCategories.DEFAULT_EXPENSE_CATEGORIES;
import static br.finax.utils.DefaultCategories.DEFAULT_REVENUE_CATEGORIES;
import static br.finax.utils.UtilsService.getAuthUser;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Category findById(long id) {
        final Category category = categoryRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        checkPermission(category);

        return category;
    }

    @Transactional(readOnly = true)
    public List<Category> getByUser() {
        return categoryRepository.findByUser(
                getAuthUser().getId()
        );
    }

    @Transactional
    public Category createNew(Category category) {
        category.setId(null);
        category.setUserId(getAuthUser().getId());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category edit(Category category) {
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
