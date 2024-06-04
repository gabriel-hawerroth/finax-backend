package br.finax.services;

import br.finax.exceptions.NotFoundException;
import br.finax.models.Category;
import br.finax.repository.CategoryRepository;
import br.finax.utils.UtilsService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final UtilsService utils;

    public List<Category> getByUser() {
        return categoryRepository.findByUser(
                utils.getAuthUser().getId()
        );
    }

    public Category getById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteById(long id) {
        try {
            categoryRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            final Category category = categoryRepository.findById(id)
                    .orElseThrow(NotFoundException::new);

            category.setActive(false);

            categoryRepository.save(category);
        }
    }

    public List<Category> findByIdIn(List<Long> categoryIds) {
        return categoryRepository.findByIdIn(categoryIds);
    }

    public void insertNewUserCategories(long userId) {
        categoryRepository.insertNewUserCategories(userId);
    }
}
