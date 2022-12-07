package com.nttdata.bc39.grupo04.product.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.nttdata.bc39.grupo04.api.product.ProductDTO;
import com.nttdata.bc39.grupo04.api.product.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/product")
public class ProductController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
	
    @Autowired
    private ProductService productService;

    @GetMapping(value = "/findAll")
    public Flux<ProductDTO> getAllProducts() {
    	LOG.error("Error, El Producto que intenta consultar es inválido");
        return productService.getAllProducts();
    }

    @GetMapping(value = "/findByCode/{code}")
    public Mono<ProductDTO> getProductByCode(@PathVariable(value = "code") String code) {
        return productService.getProductByCode(code);
    }

    @PostMapping(value = "/save")
    public Mono<ProductDTO> createProduct(@RequestBody ProductDTO body) {
    	ProductDTO.of(body);
        return productService.createProduct(body);
    }
    
    @PutMapping(value = "/update")
    public Mono<ProductDTO> updateProduct(@RequestBody ProductDTO body) {
    	ProductDTO.of(body);
    	return productService.updateProduct(body);
    }
    
    @DeleteMapping(value = "/delete/{code}")
    public Mono<Void> deleteProduct(@PathVariable(value = "code") String code) {
    	return productService.deleteProductByCode(code);
    }
}
