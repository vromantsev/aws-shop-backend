package ua.reed.mapper;

import ua.reed.dto.ProductDto;
import ua.reed.entity.Product;

public class ProductMapper implements Mapper<Product, ProductDto> {

    @Override
    public ProductDto fromSource(final Product source) {
        return fromProduct(source);
    }

    @Override
    public Product fromTarget(final ProductDto target) {
        return fromProductDto(target);
    }

    private ProductDto fromProduct(final Product product) {
        return new ProductDto(product.getDescription(), product.getId(), product.getPrice(), product.getTitle());
    }

    private Product fromProductDto(final ProductDto productDto) {
        return new Product(productDto.id(), productDto.description(), productDto.price(), productDto.title());
    }
}
