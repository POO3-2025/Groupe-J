package be.helha.poo3;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import com.google.gson.Gson;

public class ConfigReader {

    private static ConfigReader instance = null;
    private static final String FILE_PATH = "config/config.json";

    private ConfigReader() {}

    public static ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    public static ConfigurationDB readFile()throws IOException,Exception {
        ConfigurationDB config;
        InputStream fichierConfig = ConfigurationDB.class.getClassLoader().getResourceAsStream(FILE_PATH);
        Gson gson = new Gson();

        try(java.io.Reader reader = new InputStreamReader(fichierConfig, StandardCharsets.UTF_8)){
            config = gson.fromJson(reader, ConfigurationDB.class);
        }catch (NoSuchFileException message) {
            throw new IOException(message.getMessage());
        } catch (Exception e) {
            throw new Exception("Erreur lors de la lecture du fichier");
        }

        if (config == null)
            throw new Exception("La configuration est nulle");
        if( config.getDatabases().getMysql() == null) throw new ConfigInvalideException("MYSQL est nulle");
        if(config.getDatabases().getMongoDB() ==null) throw new ConfigInvalideException("MongoDB est nulle");
        return config;
    }

    public static boolean writeFile(ConfigurationDB config) {
        Gson gson = new Gson();
        String json = gson.toJson(config);

        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs(); // Crée les répertoires s'ils n'existent pas
        File parentDir = file.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            System.err.println("Impossible de créer le dossier parent : " + parentDir.getAbsolutePath());
            return false;
        }
        if (!file.canWrite()) {
            System.err.println("Permissions insuffisantes pour écrire dans le fichier : " + file.getAbsolutePath());
            return false;
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(json);
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}