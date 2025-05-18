package be.helha.poo3.serverpoo.utils;

import com.google.gson.*;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;

/**
 * Adaptateur Gson pour convertir un ObjectId de MongoDB
 * en chaîne JSON et inversement.
 */
public class ObjectIdAdapter implements JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {

    /**
     * Convertit un ObjectId en chaîne JSON.
     *
     * @param src l'ObjectId à convertir
     * @param typeOfSrc le type de l'objet (non utilisé ici)
     * @param context le contexte Gson
     * @return la chaîne JSON représentant l'ObjectId
     */
    @Override
    public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toHexString());
    }

    /**
     * Convertit une chaîne JSON en ObjectId.
     *
     * @param json la chaîne JSON à convertir
     * @param typeOfT le type attendu (non utilisé ici)
     * @param context le contexte Gson
     * @return l'ObjectId correspondant
     * @throws JsonParseException si la chaîne n'est pas valide
     */
    @Override
    public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new ObjectId(json.getAsString());
    }
}
