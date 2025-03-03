package ua.reed.repository.impl;

import ua.reed.entity.ProductWithStock;
import ua.reed.exceptions.FailedToSaveProductWithStockException;
import ua.reed.persistence.Persister;
import ua.reed.repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SimpleProductRepository implements ProductRepository {

    private final Persister persister;

    public SimpleProductRepository(final Persister persister) {
        this.persister = persister;
    }

    @Override
    public List<ProductWithStock> getProducts() {
        return this.persister.getProducts();
    }

    @Override
    public Optional<ProductWithStock> getProductById(final UUID productId) {
        return this.persister.getById(productId);
    }

    @Override
    public Optional<ProductWithStock> save(final ProductWithStock product) {
        return this.persister.putInTxReturning(product);
    }
}
