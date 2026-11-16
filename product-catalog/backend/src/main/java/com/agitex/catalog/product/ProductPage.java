package com.agitex.catalog.product;

import java.util.List;

public record ProductPage(List<ProductDto> content, int page, int size, boolean hasNext) {
}
