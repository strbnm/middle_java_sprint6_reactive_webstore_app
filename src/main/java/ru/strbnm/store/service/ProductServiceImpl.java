package ru.strbnm.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.strbnm.store.dto.ProductDTO;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.mapper.ProductMapper;
import ru.strbnm.store.repository.ProductRepository;

import java.math.BigDecimal;


@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Page<ProductDTO> findAllProductPagingAndSorting(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> findAllProductByNameOrDescriptionContaining(String search, Pageable pageable) {
        return productRepository.findProductsByNameOrDescriptionContainingIgnoreCase(search, search, pageable).map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> findAllProductByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findProductByPriceBetween(minPrice, maxPrice, pageable). map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> findAllProductByNameStartsWith(String letter, Pageable pageable) {
        return productRepository.findProductsByNameStartsWithIgnoreCase(letter, pageable).map(productMapper::toDTO);
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        Product existsProduct = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Товар не найден"));
        return productMapper.toDTO(existsProduct);
    }

    @Override
    public Product getOne(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Товар не найден"));
    }

    @Override
    public ProductDTO saveProduct(Product product) {
        return productMapper.toDTO(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }
}
