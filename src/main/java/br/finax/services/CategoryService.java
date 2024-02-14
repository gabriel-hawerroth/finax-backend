package br.finax.services;

import br.finax.models.Category;
import br.finax.models.User;
import br.finax.repository.CategoryRepository;
import br.finax.utils.UtilsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UtilsService utilsService;

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

    public List<Category> getByUser() {
        User user = utilsService.getAuthUser();
        return categoryRepository.findByUser(user.getId());
    }

    public Category save(Category category) {
        try {
            return categoryRepository.save(category);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<Void> deleteById(Long id) {
        try {
            categoryRepository.deleteById(id);
        } catch (RuntimeException e) {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

            category.setActive(false);

            categoryRepository.save(category);
        }

        return ResponseEntity.ok().build();
    }
}
