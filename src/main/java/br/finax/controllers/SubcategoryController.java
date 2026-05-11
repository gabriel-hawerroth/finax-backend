package br.finax.controllers;

import br.finax.dto.category.SaveSubcategoryDTO;
import br.finax.models.Subcategory;
import br.finax.services.SubcategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subcategory")
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    @GetMapping("/{id}")
    public ResponseEntity<Subcategory> findById(@PathVariable long id) {
        return ResponseEntity.ok(
                subcategoryService.findById(id)
        );
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<Subcategory>> getByCategoryId(@PathVariable long categoryId) {
        return ResponseEntity.ok(
                subcategoryService.getByCategoryId(categoryId)
        );
    }

    @PostMapping
    public ResponseEntity<Subcategory> createNew(@RequestBody @Valid SaveSubcategoryDTO dto) {
        final Subcategory saved = subcategoryService.createNew(dto);

        final URI uri = URI.create("/subcategory/" + saved.getId());

        return ResponseEntity.created(uri).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subcategory> edit(@PathVariable long id, @RequestBody @Valid SaveSubcategoryDTO dto) {
        return ResponseEntity.ok(
                subcategoryService.edit(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable long id) {
        subcategoryService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}

