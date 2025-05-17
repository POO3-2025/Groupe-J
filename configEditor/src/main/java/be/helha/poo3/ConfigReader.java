package be.helha.poo3;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigReader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH;

    static {
        Path jarDir;
        try{
            URI uri = ConfigReader.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path path = Paths.get(uri);
            jarDir = Files.isRegularFile(path) ? path.getParent() : path;
        } catch (URISyntaxException e){
            jarDir = Paths.get(System.getProperty("user.dir"));
        }
        FILE_PATH = jarDir.resolve("config.json");
    }

    private ConfigReader() {}


    public static ConfigurationDB readFile()throws IOException,Exception {
        if (Files.exists(FILE_PATH)) {
            try (Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
                return parse(reader);
            }
        }
        
        try (InputStream is = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config/config.json")) {
            if (is == null)
                throw new NoSuchFileException("Aucun config.json externe et aucune ressource embarquée trouvée.");
            try (Reader reader = new java.io.InputStreamReader(is, StandardCharsets.UTF_8)) {
                ConfigurationDB config = parse(reader);
                writeFile(config);
                return config;
            }
        }
    }

    private static ConfigurationDB parse(Reader reader) throws IOException, ConfigInvalideException {
        ConfigurationDB config = GSON.fromJson(reader, ConfigurationDB.class);
        if (config == null)
            throw new IOException("Configuration nulle (JSON vide ou invalide).");
        if (config.getDatabases() == null || config.getDatabases().getMysql() == null)
            throw new ConfigInvalideException("Section MySQL manquante dans le JSON");
        if (config.getDatabases().getMongoDB() == null)
            throw new ConfigInvalideException("Section MongoDB manquante dans le JSON");
        return config;
    }

    public static void writeFile(ConfigurationDB config) throws IOException {
        Files.createDirectories(FILE_PATH.getParent());

        try (var writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            GSON.toJson(config, writer);
        }
    }

    public static Path getFilePath() { return FILE_PATH; }
}