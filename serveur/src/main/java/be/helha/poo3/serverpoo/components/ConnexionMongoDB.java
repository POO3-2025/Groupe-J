package be.helha.poo3.serverpoo.components;

import be.helha.poo3.serverpoo.configuration.ConfigurationDB;
import be.helha.poo3.serverpoo.configuration.ConfigurationDB.Databases.Details;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.bson.Document;
import org.springframework.stereotype.Component;

/**
 * Composant responsable de la connexion à MongoDB à partir des paramètres
 * définis dans le fichier de configuration JSON.
 */
@Component
public class ConnexionMongoDB {

    private final ConfigurationDB configurationDB;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    // Clé d’environnement utilisée pour charger la configuration appropriée
    private static final String ENV_KEY = "production";

    public ConnexionMongoDB(ConfigurationDB configurationDB) {
        this.configurationDB = configurationDB;
    }

    /**
     * Initialise la connexion à MongoDB en utilisant la configuration correspondant à ENV_KEY.
     */
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
            mongoDatabase = mongoClient.getDatabase(config.getDatabase());

            System.out.println("Connecté à MongoDB : " + config.getDatabase());
        } catch (Exception e) {
            System.err.println("Erreur de connexion à MongoDB : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retourne une collection MongoDB à partir de son nom.
     *
     * @param name Nom de la collection à récupérer.
     * @return La collection Mongo correspondante.
     */
    public MongoCollection<Document> getCollection(String name) {
        return mongoDatabase.getCollection(name);
    }

    /**
     * Ferme proprement la connexion au client MongoDB.
     */
    @PreDestroy
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("Connexion MongoDB fermée.");
        }
    }
}