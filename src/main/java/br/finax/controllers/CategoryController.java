package br.finax.controllers;

import br.finax.models.Category;
import br.finax.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<Category> findById(@PathVariable long id) {
        return ResponseEntity.ok(
                categoryService.findById(id)
        );
    }

    @GetMapping("/get-by-user")
    public ResponseEntity<List<Category>> getByUser() {
        return ResponseEntity.ok(
                categoryService.getByUser()
        );
    }

    @PostMapping
    public ResponseEntity<Category> save(@RequestBody @Valid Category category) {
        return ResponseEntity.ok(
                categoryService.save(category)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        categoryService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
