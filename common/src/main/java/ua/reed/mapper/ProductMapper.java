package ua.reed.mapper;

import ua.reed.dto.ProductDto;
import ua.reed.entity.ProductWithStock;

public class ProductMapper implements Mapper<ProductWithStock, ProductDto> {

    @Override
    public ProductDto fromSource(final ProductWithStock source) {
        return fromProduct(source);
    }

    @Override
    public ProductWithStock fromTarget(final ProductDto target) {
        return fromProductDto(target);
    }

    private ProductDto fromProduct(final ProductWithStock product) {
        return new ProductDto(product.getDescription(), product.getId(), product.getPrice(), product.getTitle(), product.getCount());
    }

    private ProductWithStock fromProductDto(final ProductDto productDto) {
        return new ProductWithStock(productDto.id(), productDto.description(), productDto.price(), productDto.title(), productDto.count());
    }
}
