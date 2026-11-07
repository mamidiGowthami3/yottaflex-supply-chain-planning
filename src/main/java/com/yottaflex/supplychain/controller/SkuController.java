package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.entity.SKU;
import com.yottaflex.supplychain.service.SkuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skus")
public class SkuController {

    private final SkuService skuService;

    public SkuController(SkuService skuService) {
        this.skuService = skuService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SKU>>> getAllSkus() {
        return ResponseEntity.ok(ApiResponse.success(skuService.findAll()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SKU>>> getActiveSkus() {
        return ResponseEntity.ok(ApiResponse.success(skuService.findActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SKU>> getSkuById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(skuService.findById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<SKU>> getSkuByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(skuService.findBySkuCode(code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SKU>> createSku(@Valid @RequestBody SKU sku) {
        SKU created = skuService.create(sku);
        return new ResponseEntity<>(ApiResponse.success("SKU created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SKU>> updateSku(@PathVariable Long id, @Valid @RequestBody SKU sku) {
        SKU updated = skuService.update(id, sku);
        return ResponseEntity.ok(ApiResponse.success("SKU updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSku(@PathVariable Long id) {
        skuService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("SKU deleted successfully", null));
    }
}

