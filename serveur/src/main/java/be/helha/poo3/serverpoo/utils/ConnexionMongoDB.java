package be.helha.poo3.serverpoo.utils;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ConnexionMongoDB {
    private static final String URI = "mongodb://game:password@localhost:27017";
    private static final String DATABASE_NAME = "TestDB";
    private static final String COLLECTION_NAME = "MaCollection";

    private static ConnexionMongoDB instance;
    private MongoCollection<Document> collection;
    private MongoClient mongoClient;

    private ConnexionMongoDB() {
        try {
            mongoClient = MongoClients.create(URI);
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            this.collection = database.getCollection(COLLECTION_NAME);
            System.out.println("Connexion réussie à la base de données MongoDB.");
        } catch (Exception e) {
            System.out.println("Une erreur est survenue lors de la connexion.");
            e.printStackTrace();
        }
    }

    public static ConnexionMongoDB getInstance() {
        if (instance == null) {
            synchronized (ConnexionMongoDB.class) {
                if (instance == null) {
                    instance = new ConnexionMongoDB();
                }
            }
        }
        return instance;
    }

    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("Connexion MongoDB fermée.");
        }
    }

    public void insertDoc(Document doc) {
        try {
            InsertOneResult result = collection.insertOne(doc);
            ObjectId id = result.getInsertedId().asObjectId().getValue();
            System.out.println("Document inséré avec ObjectId: " + id.toHexString());
        } catch (Exception e) {
            System.out.println("Erreur lors de l'insertion.");
            e.printStackTrace();
        }
    }

    public void readDocs() {
        System.out.println("Lecture des documents dans MongoDB:");
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                System.out.println(cursor.next().toJson());
            }
        }
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void deleteDoc(String nom) {
        try {
            DeleteResult result = collection.deleteOne(Filters.eq("Name", nom));
            if (result.getDeletedCount() > 0) {
                System.out.println("Document supprimé avec succès.");
            } else {
                System.out.println("Aucun document trouvé avec ce critère.");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression.");
            e.printStackTrace();
        }
    }
}
