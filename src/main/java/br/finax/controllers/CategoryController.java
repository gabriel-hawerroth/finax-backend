package br.finax.controllers;

import br.finax.models.Category;
import br.finax.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/{id}")
    private Category getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
    }

    @GetMapping("/get-by-user/{userId}")
    private List<Category> getByUser(@PathVariable Long userId) {
        return categoryRepository.findByUser(userId);
    }

    @PostMapping
    private Category save(@RequestBody Category category) {
        try {
            return categoryRepository.save(category);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteById(@PathVariable Long id) {
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
