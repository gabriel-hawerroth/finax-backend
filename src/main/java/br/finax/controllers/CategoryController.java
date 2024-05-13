package br.finax.controllers;

import br.finax.models.Category;
import br.finax.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/get-by-user")
    public List<Category> getByUser() {
        return categoryService.getByUser();
    }

    @GetMapping("/{id}")
    public Category getById(@PathVariable long id) {
        return categoryService.getById(id);
    }

    @PostMapping
    public Category save(@RequestBody @Valid Category category) {
        return categoryService.save(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        return categoryService.deleteById(id);
    }
}
