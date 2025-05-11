package be.helha.poo3.serverpoo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bson.types.ObjectId;

/**
 * Fournit un objet Gson configuré pour gérer les ObjectId de MongoDB.
 */
public class GsonObjectIdAdapted {

    /**
     * Retourne une instance de Gson avec un adaptateur ObjectId.
     *
     * @return un Gson prêt à (dé)sérialiser des ObjectId
     */
    public static Gson getGson() {
        Gson adaptedGson = new GsonBuilder()
                .registerTypeAdapter(ObjectId.class, new ObjectIdAdapter())
                .create();
        return adaptedGson;
    }
}
