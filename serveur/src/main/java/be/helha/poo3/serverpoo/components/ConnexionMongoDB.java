package be.helha.poo3.serverpoo.components;

import be.helha.poo3.serverpoo.configuration.MongoConfig;
import com.mongodb.client.*;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ConnexionMongoDB {
    private final MongoConfig config;
    protected MongoCollection<Document> collection;
    private MongoClient mongoClient;

    public ConnexionMongoDB(MongoConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        String fullUri = String.format("mongodb://%s:%s@localhost:27017", config.getUsername(), config.getPassword());
        mongoClient = MongoClients.create(fullUri);
        MongoDatabase database = mongoClient.getDatabase(config.getDb());
        this.collection = database.getCollection(config.getCollection());
        System.out.println("Connected to " + config.getDb() + "/" + config.getCollection());
    }

    public MongoCollection<Document> getCollection(String name) {
        return mongoClient.getDatabase(config.getDb()).getCollection(name);
    }
}

