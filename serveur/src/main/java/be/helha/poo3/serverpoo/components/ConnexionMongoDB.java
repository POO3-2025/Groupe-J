package be.helha.poo3.serverpoo.component;

import be.helha.poo3.serverpoo.configuration.MongoConfig;
import com.mongodb.client.*;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ConnexionMongoDB {
    private final MongoConfig config;

    private MongoCollection<Document> collection;
    private MongoClient mongoClient;


    private ConnexionMongoDB(MongoConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        mongoClient = MongoClients.create(config.getUri());
        MongoDatabase database = mongoClient.getDatabase(config.getDb());
        this.collection = database.getCollection(config.getCollection());
        System.out.println("Connected to " + config.getDb()+"/"+config.getCollection());
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }
}
