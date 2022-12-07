package com.nttdata.bc39.grupo04.customer;

import com.nttdata.bc39.grupo04.customer.persistence.CustomerEntity;
import org.apache.log4j.BasicConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;

@SpringBootApplication
@EnableEurekaClient
@ComponentScan({"com.nttdata.bc39.grupo04"})
public class CustomerServiceApplication {
    @Autowired
    private ReactiveMongoOperations mongoTemplate;

    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartUp() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext =
                mongoTemplate.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

        ReactiveIndexOperations indexOps = mongoTemplate.indexOps(CustomerEntity.class);
        resolver.resolveIndexFor(CustomerEntity.class).forEach(e -> indexOps.ensureIndex(e).block());
    }

}
