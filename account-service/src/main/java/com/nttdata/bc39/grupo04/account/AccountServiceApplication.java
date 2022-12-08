package com.nttdata.bc39.grupo04.account;

import com.nttdata.bc39.grupo04.account.persistence.AccountEntity;
import org.apache.log4j.BasicConfigurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
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
@ComponentScan("com.nttdata.bc39.grupo04")
@EnableFeignClients("com.nttdata.bc39.grupo04.api.feign")
public class AccountServiceApplication {
    @Autowired
    private ReactiveMongoOperations mongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
        BasicConfigurator.configure();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartUp() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext =
                mongoTemplate.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

        ReactiveIndexOperations indexOps = mongoTemplate.indexOps(AccountEntity.class);
        resolver.resolveIndexFor(AccountEntity.class).forEach(e -> indexOps.ensureIndex(e).block());
    }

}
