package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.exceptions.InventoryIOException;
import be.helha.poo3.serverpoo.models.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Primary
@Service
public class CharacterService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ItemLoaderService itemLoaderService;

    /**
     * Récupère une liste de personnages appartenant à l'utilisateur dont l'ID est spécifié dans la base de données.
     *
     * @param userId l'ID de l'utilisateur à qui appartiennent les personnages à rechercher
     * @return une liste de GameCharacter correspondant à l'utilisateur, ou une liste vide si aucun personnage n'est trouvé
     * @throws RuntimeException si une erreur survient lors de l'exécution de la requête SQL
     */
    public List<GameCharacter> getCharactersByUser(int userId) {
        String sql = "SELECT * FROM `character` WHERE idUser = ?";

        List<GameCharacter> GameCharacterList = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                GameCharacter character = new GameCharacter(
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
                GameCharacterList.add(character);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des personnages : "+e.getMessage(), e);
        }
        return GameCharacterList;
    }


    /**
     * Récupère le personnage correspondant à l'ID spécifié dans la base de données.
     *
     * @param characterId l'ID du personnage à rechercher
     * @return un objet GameCharacter correspondant à l'ID, ou null si aucun personnage n'est trouvé
     * @throws RuntimeException si une erreur survient lors de l'exécution de la requête SQL
     */
    public GameCharacter getCharacterById(int characterId) {
        String sql = "SELECT * FROM `character` WHERE idCharacter = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, characterId);

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
                    throw new IllegalArgumentException("No character with id " + characterId + " found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du personnage : ", e);
        }
    }

    /**
     * Ajoute un nouveau personnage dans la base de données et met à jour son identifiant auto-généré et l'id de son inventaire.
     *
     * @param character le personnage à ajouter
     * @return l'objet {@code GameCharacter} inséré, avec son nouvel ID et l'id de son inventaire mis à jour
     * @throws RuntimeException en cas d'erreur lors de l'insertion en base de données
     */
    public GameCharacter addCharacter(GameCharacter character,String classe) {
        if (characterExistsByName(character.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nom est déjà utilisé");
        }

        Inventory inventory = inventoryService.createInventory();
        character.setInventoryId(inventory.getId().toString());
        String weaponName;
        String armorName;
        Item weapon;
        Item armor;

        switch(classe){
            case "mage":
                weaponName = "Simple Staff";
                armorName = "Simple Robe";
                break;
            case "warrior":
                weaponName = "Basic Sword";
                armorName = "Chainmail Armor";
                break;
            case "hunter":
                weaponName = "Basic Bow";
                armorName = "Rude Leather Armor";
                break;
            default:
                throw new IllegalArgumentException("Class "+ classe + " does not exist");
        }
        weapon = itemLoaderService.findByName(weaponName);
        armor = itemLoaderService.findByName(armorName);
        if (weapon == null){
            throw new RuntimeException(weaponName + " not found");
        } else if (armor == null){
            throw new RuntimeException(armorName + " not found");
        }

        String sql = "INSERT INTO `character` (idUser, name, inventoryId, maxHP, currentHP, constitution, dexterity, strength) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";


        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, character.getIdUser());
            stmt.setString(2, character.getName());
            stmt.setString(3, character.getInventoryId());
            stmt.setInt(4, character.getMaxHP());
            stmt.setInt(5, character.getCurrentHP());
            stmt.setInt(6, character.getConstitution());
            stmt.setInt(7, character.getDexterity());
            stmt.setInt(8, character.getStrength());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    character.setIdCharacter(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion du personnage : "+e.getMessage(), e);
        }
        weapon = inventoryService.addItemToInventory(inventory.getId(),weapon.getId());
        armor = inventoryService.addItemToInventory(inventory.getId(),armor.getId());
        try {
            inventoryService.pushToSlot(inventory.getId(),"main",weapon);
            inventoryService.pushToSlot(inventory.getId(),"armor",armor);
        } catch (InventoryIOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return character;
    }

    /**
     * Met à jour le nom d'un personnage en base de données
     *
     * @param characterId l'ID du personnage à modifier
     * @param name le nouveau nom du personnage
     * @return un booléen indiquant si le changement a été effectué ou non(en cas de nom déjà utilisé)
     * @throws RuntimeException si le personnage à modifier n'existe pas
     */
    public Boolean updateCharacterName(int characterId, String name) throws IllegalArgumentException {
        if(characterExistsByName(name)) throw new IllegalArgumentException("Le nom est déjà utilisé");
        String sql = "UPDATE `character` SET name = ? WHERE idCharacter = ?";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, name);
            stmt.setInt(2, characterId);
            int rows = stmt.executeUpdate();          // <‑‑ executeUpdate ici
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du changement de nom \""+ name +"\": "+ e.getMessage(), e);
        }
    }

    /**
     * Désactive le personnage dont l'ID est spécifié en le supprimant dans la base de données.
     *
     * @param characterId L'ID du personnage à supprimer
     * @throws RuntimeException en cas d'erreur SQL ou si le personnage n'a pas pu être supprimé.
     */
    public void deleteCharacterById(int characterId) {
        String sql = "DELETE FROM `character` WHERE idCharacter = ?";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, characterId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du personnage", e);
        }
    }

    /**
     * Vérifie que le personnage dont l'id est spécifié appartient à l'utilisateur dont l'id est spécifier .
     *
     * @param userId l'ID de l'utilisateur
     * @param characterId l'ID du personnage
     * @return un booléen indiquant si le personnage appartient à l'utilisateur
     * @throws RuntimeException si le personnage à rechercher n'existe pas
     */
    public boolean userOwnsCharacter(int userId, int characterId) {
        String sql = "SELECT COUNT(*) FROM `character` WHERE idCharacter = ? AND idUser = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, characterId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du personnage", e);
        }
        return false;
    }

    /**
     * Vérifie que le personnage dont le nom est spécifié existe en base de données
     *
     * @param characterName le nom du personnage
     * @return un booléen indiquant si le personnage existe
     * @throws RuntimeException si le personnage à rechercher n'existe pas
     */
    public boolean characterExistsByName(String characterName) {
        String sql = "SELECT COUNT(*) FROM `character` WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, characterName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du nom du personnage", e);
        }
        return false;
    }

    /**
     * Vérifie que le personnage dont l'ID est spécifié existe en base de données
     *
     * @param characterId l'ID' du personnage
     * @return un booléen indiquant si le personnage existe
     * @throws RuntimeException si le personnage à rechercher n'existe pas
     */
    public boolean characterExistsById(int characterId) {
        String sql = "SELECT COUNT(*) FROM `character` WHERE idCharacter = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, characterId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du personnage", e);
        }
        return false;
    }

    /**
     * Met à jour les points de vie actuels d’un personnage en base de données.
     */
    public void updateCurrentHP(int characterId, int currentHP) {
        String sql = "UPDATE `character` SET currentHP = ? WHERE idCharacter = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentHP);
            stmt.setInt(2, characterId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour des HP : " + e.getMessage(), e);
        }
    }
}