package com.logistics.intellistock.service.impl;

import com.logistics.intellistock.dto.request.CreateProductRequest;
import com.logistics.intellistock.dto.request.UpdateProductRequest;
import com.logistics.intellistock.dto.response.ProductAdminResponse;
import com.logistics.intellistock.dto.response.ProductResponse;
import com.logistics.intellistock.entity.Product;
import com.logistics.intellistock.entity.enums.Category;
import com.logistics.intellistock.core.exception.DuplicateResourceException;
import com.logistics.intellistock.core.exception.ResourceNotFoundException;
import com.logistics.intellistock.mapper.ProductMapper;
import com.logistics.intellistock.repository.ProductRepository;
import com.logistics.intellistock.service.EncryptionService;
import com.logistics.intellistock.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final EncryptionService encryptionService;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        // Check if SKU already exists
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "SKU", request.getSku());
        }

        try {
            BigDecimal margin = request.getSellingPrice().subtract(request.getPurchasePrice());

            Product product = Product.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .category(request.getCategory())
                    .sku(request.getSku())
                    .sellingPrice(request.getSellingPrice())
                    .purchasePrice(request.getPurchasePrice())
                    .margin(margin)
                    .weight(request.getWeight())
                    .unit(request.getUnit())
                    .isActive(true)
                    .build();

            Product savedProduct = productRepository.save(product);
            log.info("Product created successfully: {}", savedProduct.getSku());

            return productMapper.toResponse(savedProduct);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            throw new RuntimeException("Failed to create product", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAdminResponse getProductByIdAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toAdminResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findByIsActive(true);
        return productMapper.toResponseList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductAdminResponse> getAllProductsAdmin() {
        List<Product> products = productRepository.findByIsActive(true);

        return productMapper.toAdminResponseList(products);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Category category) {
        List<Product> products = productRepository.findByCategory(category);
        return productMapper.toResponseList(products);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        try {
            // Update basic fields
            if (request.getName() != null) product.setName(request.getName());
            if (request.getDescription() != null) product.setDescription(request.getDescription());
            if (request.getCategory() != null) product.setCategory(request.getCategory());
            if (request.getSellingPrice() != null) product.setSellingPrice(request.getSellingPrice());
            if (request.getPurchasePrice() != null) product.setPurchasePrice(request.getPurchasePrice());
            if (request.getWeight() != null) product.setWeight(request.getWeight());
            if (request.getUnit() != null) product.setUnit(request.getUnit());
            if (request.getIsActive() != null) product.setIsActive(request.getIsActive());

            product.setMargin(product.getSellingPrice().subtract(product.getPurchasePrice()));

            Product updatedProduct = productRepository.save(product);
            log.info("Product updated successfully: {}", updatedProduct.getSku());

            return productMapper.toResponse(updatedProduct);
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage());
            throw new RuntimeException("Failed to update product", e);
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Soft delete
        product.setIsActive(false);
        productRepository.save(product);

        log.info("Product deleted successfully: {}", product.getSku());
    }
}
