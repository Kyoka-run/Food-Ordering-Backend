package com.kyoka.controller;

import com.kyoka.Util.AuthUtil;
import com.kyoka.dto.request.CategoryDTO;
import com.kyoka.dto.response.APIResponse;
import com.kyoka.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/admin/category")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(createdCategory);
    }

    @GetMapping("/category/restaurant/{restaurantId}")
    public ResponseEntity<List<CategoryDTO>> getRestaurantCategories(@PathVariable Long restaurantId) {
        List<CategoryDTO> categories = categoryService.findCategoryByRestaurantId(restaurantId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/admin/category/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.findCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/admin/category/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/admin/category/{id}")
    public ResponseEntity<APIResponse> deleteCategory(@PathVariable Long id) {
        String message = categoryService.deleteCategory(id);
        APIResponse response = new APIResponse(message, true);
        return ResponseEntity.ok(response);
    }
}