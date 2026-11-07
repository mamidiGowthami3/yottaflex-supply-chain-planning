package com.yottaflex.supplychain.controller;

import com.yottaflex.supplychain.dto.ApiResponse;
import com.yottaflex.supplychain.entity.Supplier;
import com.yottaflex.supplychain.service.SupplierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Supplier>>> getAllSuppliers() {
        return ResponseEntity.ok(ApiResponse.success(supplierService.findAll()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Supplier>>> getActiveSuppliers() {
        return ResponseEntity.ok(ApiResponse.success(supplierService.findActive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(supplierService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Supplier>> createSupplier(@Valid @RequestBody Supplier supplier) {
        Supplier created = supplierService.create(supplier);
        return new ResponseEntity<>(ApiResponse.success("Supplier created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> updateSupplier(@PathVariable Long id, @Valid @RequestBody Supplier supplier) {
        Supplier updated = supplierService.update(id, supplier);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted successfully", null));
    }
}

