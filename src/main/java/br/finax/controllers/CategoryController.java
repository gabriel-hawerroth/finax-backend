package br.finax.controllers;

import br.finax.models.Category;
import br.finax.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
    public ResponseEntity<Category> createNew(@RequestBody @Valid Category category) {
        final Category savedCategory = categoryService.createNew(category);

        final URI uri = URI.create("/category/" + savedCategory.getId());

        return ResponseEntity.created(uri).body(category);
    }

    @PutMapping
    public ResponseEntity<Category> edit(@RequestBody @Valid Category category) {
        return ResponseEntity.ok(
                categoryService.edit(category)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        categoryService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
