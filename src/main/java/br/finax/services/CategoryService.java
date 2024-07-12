package br.finax.services;

import br.finax.exceptions.NotFoundException;
import br.finax.exceptions.WithoutPermissionException;
import br.finax.models.Category;
import br.finax.repository.CategoryRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.finax.utils.DefaultCategories.DEFAULT_EXPENSE_CATEGORIES;
import static br.finax.utils.DefaultCategories.DEFAULT_REVENUE_CATEGORIES;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UtilsService utils;

    @Transactional(readOnly = true)
    public Category findById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<Category> getByUser() {
        return categoryRepository.findByUser(
                utils.getAuthUser().getId()
        );
    }

    @Transactional
    public Category save(Category category) {
        category.setUserId(utils.getAuthUser().getId());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteById(long id) {
        try {
            if (findById(id).getUserId() != utils.getAuthUser().getId())
                throw new WithoutPermissionException();

            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            final Category category = findById(id);

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
}
