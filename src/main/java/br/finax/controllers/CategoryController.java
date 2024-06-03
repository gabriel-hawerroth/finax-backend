package br.finax.controllers;

import br.finax.models.Category;
import br.finax.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/get-by-user")
    public ResponseEntity<List<Category>> getByUser() {
        return ResponseEntity.ok(
                categoryService.getByUser()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable long id) {
        return ResponseEntity.ok(
                categoryService.getById(id)
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

        return ResponseEntity.ok().build();
    }
}
