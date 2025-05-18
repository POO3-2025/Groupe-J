package be.helha.poo3.serverpoo.services;


import be.helha.poo3.serverpoo.models.CharacterWithPos;
import be.helha.poo3.serverpoo.models.GameCharacter;
import be.helha.poo3.serverpoo.models.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Primary
@Service
public class InGameCharacterService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DungeonMapService dungeonMapService;

    private final List<CharacterWithPos> loadedCharacters = new ArrayList<>();

    public List<CharacterWithPos> getLoadedCharacters(){
        return loadedCharacters;
    }

    /**
     * Ajoute un personnage à la liste des personnages en jeu.
     *
     * @param character le personnage à ajouter
     * @return l'objet {@code CharacterWithPos} ajouté, avec sa position choisie aléatoirement
     */
    public CharacterWithPos addCharacterInGame(GameCharacter character) {
        Collection<Room> rooms = dungeonMapService.getAllRooms();
        int r = ThreadLocalRandom.current().nextInt(rooms.size());
        Room randomRoom = dungeonMapService.getAllRooms().stream().skip(r).findFirst().orElseThrow();
        CharacterWithPos characterWithPos = new CharacterWithPos(
                character.getIdCharacter(),
                character.getIdUser(),
                character.getName(),
                character.getInventoryId(),
                character.getMaxHP(),
                character.getCurrentHP(),
                character.getConstitution(),
                character.getDexterity(),
                character.getStrength(),
                new Point(randomRoom.getX(), randomRoom.getY())
        );
        characterWithPos.setLastAction();
        loadedCharacters.add(characterWithPos);
        return characterWithPos;
    }

    /**
     * retire un personnage de la liste des personnages en jeu.
     *
     * @param characterId l'id du personnage à retirer
     * @return un booléen indiquant si le retrait a été effectué avec succès
     */
    public boolean removeCharacterFromGame(int characterId) {
        return loadedCharacters.removeIf(c -> c.getIdCharacter() == characterId);
    }

    /**
     * Récupère le personnage correspondant à l'ID spécifié dans la liste des personnages en jeu.
     *
     * @param characterId l'ID du personnage
     * @return un objet {@code CharacterWithPos} correspondant
     * @throws RuntimeException si l'utilisateur n'a fait aucune action depuis un certain temps, l'excluant du donjon
     * @throws IllegalArgumentException si aucune personnage n'a été trouvé avec l'id spécifié
     */
    public CharacterWithPos getCharacterFromGame(int characterId) throws RuntimeException {
        CharacterWithPos character = loadedCharacters.stream().filter(c -> c.getIdCharacter() == characterId).findFirst().orElse(null);
        if (character == null) {
            throw new IllegalArgumentException("No character with id " + characterId + " found");
        } else if (character.hasActedRecently(20)) {
            return character;
        } else {
            removeCharacterFromGame(character.getIdCharacter());
            throw new RuntimeException("User was AFK for to much time");
        }
    }

    /**
     * Récupère le personnage appartenant à l'utisateur dont l'ID est spécifié dans la liste des personnages en jeu.
     *
     * @param userId l'ID de l'utilisateur
     * @return un objet {@code CharacterWithPos} correspondant
     * @throws RuntimeException si l'utilisateur n'a fait aucune action depuis un certain temps, l'excluant du donjon
     * @throws IllegalArgumentException si aucune personnage n'a été trouvé pour l'utilisateur spécifié
     */
    public CharacterWithPos getInGameCharacterByUserId(int userId) {
        CharacterWithPos character = loadedCharacters.stream().filter(c -> c.getIdUser() == userId).findFirst().orElse(null);
        if (character == null) {
            return null;
        } else if (character.hasActedRecently(20)) {
            return character;
        } else {
            removeCharacterFromGame(character.getIdCharacter());
            throw new RuntimeException("User was AFK for to much time");
        }
    }

    /**
     * Récupère le personnage actif correspondant à l'ID spécifié dans la base de données.
     *
     * @param userId l'ID de l'utilisateur
     * @return un objet GameCharacter correspondant
     * @throws IllegalArgumentException is aucun personnage récent n'a été trouvé pour l'utilisateur
     * @throws RuntimeException si une erreur survient lors de l'exécution de la requête SQL
     */
    public GameCharacter getLastCharacter(int userId) throws RuntimeException {
        String sql = "SELECT c.* FROM `user` u JOIN `character` c ON c.idCharacter = u.idLastCharacter WHERE u.id_user = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new GameCharacter(
                            rs.getInt("idCharacter"),
                            rs.getInt("idUser"),
                            rs.getString("name"),
                            rs.getString("inventoryId"),
                            rs.getInt("maxHP"),
                            rs.getInt("currentHP"),
                            rs.getInt("constitution"),
                            rs.getInt("dexterity"),
                            rs.getInt("strength")
                    );
                } else {
                    throw new IllegalArgumentException("Aucun personnage trouvé");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du personnage : "+ e.getMessage(), e);
        }
    }
}
