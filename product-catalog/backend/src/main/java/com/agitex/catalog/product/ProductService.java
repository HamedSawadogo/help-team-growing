package com.agitex.catalog.product;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ProductPage findPage(int page, int size) {
        var ids = repository.findIds(PageRequest.of(page, size, Sort.by("id")));
        var byId = repository.findByIdIn(ids.getContent()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        var content = ids.getContent().stream()
                .map(byId::get)
                .map(ProductDto::from)
                .toList();
        return new ProductPage(content, page, size, ids.hasNext());
    }
}
