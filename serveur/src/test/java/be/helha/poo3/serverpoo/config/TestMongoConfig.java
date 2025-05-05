package be.helha.poo3.serverpoo.config;

import be.helha.poo3.serverpoo.configuration.MongoConfig;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoClientSettings;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;

@TestConfiguration
@Import(MongoConfig.class)
public class TestMongoConfig {

    @Bean
    public MongoClient mongoClient(MongoConfig config) {
        MongoCredential credential = MongoCredential.createCredential(
                config.getUsername(),
                config.getDb(),
                config.getPassword().toCharArray()
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress("localhost", 27017))))
                .credential(credential)
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient, MongoConfig config) {
        return new MongoTemplate(mongoClient, config.getDb());
    }
}
