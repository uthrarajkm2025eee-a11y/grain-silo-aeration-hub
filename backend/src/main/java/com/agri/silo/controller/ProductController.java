package com.agri.silo.controller;

import com.agri.silo.dto.ProductDTO;
import com.agri.silo.service.MasterDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final MasterDataService masterDataService;

    public ProductController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts() {
        return ResponseEntity.ok(masterDataService.getProducts());
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO dto) {
        ProductDTO created = masterDataService.createProduct(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
