package com.nttdata.bc39.grupo04.api.product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
    Flux<ProductDTO> getAllProducts();

    Mono<ProductDTO> getProductByCode(String code);

    Mono<ProductDTO> createProduct(ProductDTO dto);

    Mono<ProductDTO> updateProduct(ProductDTO dto);

    Mono<Void> deleteProductByCode(String code);
}
