package com.agitex.catalog.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record ProductDto(Long id,
                         String name,
                         String description,
                         BigDecimal price,
                         LocalDateTime createdAt,
                         CategoryDto category,
                         List<TagDto> tags) {

    public record CategoryDto(Long id, String name) {
    }

    public record TagDto(Long id, String name) {
    }

    static ProductDto from(Product p) {
        var category = p.getCategory() == null ? null
                : new CategoryDto(p.getCategory().getId(), p.getCategory().getName());
        var tags = p.getTags().stream()
                .map(t -> new TagDto(t.getId(), t.getName()))
                .sorted(Comparator.comparing(TagDto::name))
                .toList();
        return new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCreatedAt(), category, tags);
    }
}
