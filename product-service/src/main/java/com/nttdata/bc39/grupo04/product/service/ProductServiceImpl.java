package com.nttdata.bc39.grupo04.product.service;

import com.nttdata.bc39.grupo04.api.product.ProductDTO;
import com.nttdata.bc39.grupo04.api.product.ProductService;
import com.nttdata.bc39.grupo04.api.product.exception.ServiceException;
import com.nttdata.bc39.grupo04.product.persistence.ProductEntity;
import com.nttdata.bc39.grupo04.product.persistence.ProductRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class ProductServiceImpl implements ProductService {

    private ProductRepository repository;
    private ProductMapper mapper;
    private static final Logger LOG = Logger.getLogger(ProductServiceImpl.class);

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<ProductDTO> getAllProducts() {
        return repository.findAll().map(mapper::entityToDto);
    }

    @Override
    public Mono<ProductDTO> getProductByCode(String code) {
        if (Objects.isNull(code)) {
            LOG.info("Error, El Producto que intenta consultar es inválido");
            throw new ServiceException("Error, El Producto que intenta consultar es invalido",
                    HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
        }

        Mono<ProductDTO> productDTO = repository.findAll().filter(x -> x.getCode().equals(code)).next().map(mapper::entityToDto);
        if (Objects.isNull(productDTO.block())) {
            LOG.info("Error, El Producto que intenta buscar no existe");
            throw new ServiceException("Error, El Producto que intenta buscar no existe",
                    HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
        }
        return productDTO;
    }

    @Override
    public Mono<ProductDTO> createProduct(ProductDTO dto) {
        validateCreateProduct(dto);
        ProductEntity entity = mapper.dtoToEntity(dto);
        return repository.save(entity).map(mapper::entityToDto);
    }

    @Override
    public Mono<ProductDTO> updateProduct(ProductDTO dto) {
        // TODO Auto-generated method stub
        Mono<ProductEntity> productEntity = repository.findAll().filter(x -> x.getCode().equals(dto.getCode())).next();
        ProductEntity productEntityNew = productEntity.block();

        if (Objects.isNull(productEntityNew)) {
            LOG.info("Error, El Producto que intenta modificar no existe");
            throw new ServiceException("Error, El Producto que intenta modificar no existe",
                    HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
        }
        productEntityNew.setName(dto.getName());
        productEntityNew.setTypeProduct(dto.getTypeProduct());
        return repository.save(productEntityNew).map(mapper::entityToDto);
    }

    @Override
    public Mono<Void> deleteProductByCode(String code) {
        Mono<ProductEntity> productEntity = repository.findAll().filter(x -> x.getCode().equals(code)).next();
        ProductEntity productEntityNew = productEntity.block();
        if (Objects.isNull(productEntityNew)) {
            LOG.info("Error, El Producto que intenta eliminar no existe");
            throw new ServiceException("Error, El Producto que intenta eliminar no existe",
                    HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
        }
        return repository.delete(productEntityNew);
    }

    private void validateCreateProduct(ProductDTO dto) {
        Mono<ProductEntity> productEntity = repository.findAll().filter(x -> x.getCode().equals(dto.getCode())).next();
        ProductEntity productEntityNew = productEntity.block();

        if (!Objects.isNull(productEntityNew)) {
            LOG.info("Error, Ya existe un Producto registrado con el mismo código");
            throw new ServiceException("Error, Ya existe un Producto registrado con el mismo código",
                    HttpStatus.NOT_FOUND.toString(), LocalDate.now().toString(), HttpStatus.NOT_FOUND.value());
        }
    }

}
