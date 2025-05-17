// Fichier : ConnexionMongoDB.java
package be.helha.poo3.serverpoo.components;

import be.helha.poo3.serverpoo.configuration.ConfigurationDB;
import be.helha.poo3.serverpoo.configuration.ConfigurationDB.Databases.Details;
import com.mongodb.client.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ConnexionMongoDB {

    private final ConfigurationDB configurationDB;
    private MongoClient mongoClient;
    protected MongoCollection<Document> collection;

    // "production" ou "test" selon ton contexte
    private static final String ENV_KEY = "production";

    public ConnexionMongoDB(ConfigurationDB configurationDB) {
        this.configurationDB = configurationDB;
    }

    @PostConstruct
    public void init() {
        try {
            Details config = configurationDB.getDatabases().getMongoDB().get(ENV_KEY);

            String uri = String.format(
                    "mongodb://%s:%s@%s:%d",
                    config.getUser(),
                    config.getPassword(),
                    config.getHost(),
                    config.getPort()
            );

            mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase(config.getDatabase());
            this.collection = database.getCollection("exampleCollection"); // change le nom si besoin

            System.out.println("Connecté à MongoDB: " + config.getDatabase());
        } catch (Exception e) {
            System.err.println("Erreur de la connexion à MongoDB: " + e.getMessage());
        }
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    @PreDestroy
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoClient closed.");
        }
    }
}
