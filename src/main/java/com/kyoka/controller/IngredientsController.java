package com.kyoka.controller;

import com.kyoka.dto.IngredientCategoryDTO;
import com.kyoka.dto.IngredientsItemDTO;
import com.kyoka.service.IngredientsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ingredients")
public class IngredientsController {

    @Autowired
    private IngredientsService ingredientsService;

    @PostMapping("/category")
    public ResponseEntity<IngredientCategoryDTO> createIngredientCategory(@RequestBody IngredientCategoryDTO categoryDTO) {
        IngredientCategoryDTO createdCategory = ingredientsService.createIngredientsCategory(categoryDTO);
        return ResponseEntity.ok(createdCategory);
    }

    @PostMapping
    public ResponseEntity<IngredientsItemDTO> createIngredient(@RequestBody IngredientsItemDTO itemDTO) {
        IngredientsItemDTO createdItem = ingredientsService.createIngredientsItem(itemDTO);
        return ResponseEntity.ok(createdItem);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<IngredientsItemDTO> updateStock(@PathVariable Long id) {
        IngredientsItemDTO updatedItem = ingredientsService.updateStock(id);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/restaurant/{id}")
    public ResponseEntity<List<IngredientsItemDTO>> getRestaurantsIngredients(@PathVariable Long id) {
        List<IngredientsItemDTO> items = ingredientsService.findRestaurantsIngredientItems(id);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/restaurant/{id}/category")
    public ResponseEntity<List<IngredientCategoryDTO>> getRestaurantsIngredientCategories(@PathVariable Long id) {
        List<IngredientCategoryDTO> categories = ingredientsService.findIngredientsCategoryByRestaurantId(id);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<IngredientCategoryDTO> updateIngredientCategory(
            @PathVariable Long id,
            @RequestBody IngredientCategoryDTO categoryDTO) {
        IngredientCategoryDTO updatedCategory = ingredientsService.updateIngredientsCategory(id, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientsItemDTO> updateIngredient(
            @PathVariable Long id,
            @RequestBody IngredientsItemDTO itemDTO) {
        IngredientsItemDTO updatedItem = ingredientsService.updateIngredientsItem(id, itemDTO);
        return ResponseEntity.ok(updatedItem);
    }
}