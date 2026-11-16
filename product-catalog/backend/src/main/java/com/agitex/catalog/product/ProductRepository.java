package com.agitex.catalog.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Pagination sur les ids seuls : paginer une requête avec "fetch join" sur une collection
    // forcerait Hibernate à paginer en mémoire.
    @Query("select p.id from Product p")
    Slice<Long> findIds(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags"})
    List<Product> findByIdIn(Collection<Long> ids);
}
