package be.helha.poo3.serverpoo.services;


import be.helha.poo3.serverpoo.models.CharacterWithPos;
import be.helha.poo3.serverpoo.models.GameCharacter;
import be.helha.poo3.serverpoo.models.Inventory;
import be.helha.poo3.serverpoo.models.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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

    private List<CharacterWithPos> loadedCharacters;

    public List<GameCharacter> getCharacters(int userId) {
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


    public GameCharacter getCharacterById(int id) {
        String sql = "SELECT * FROM `character` WHERE idCharacter = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

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
                    throw new IllegalArgumentException("No character with id " + id + " found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du personnage : ", e);
        }
    }

    public GameCharacter addCharacter(GameCharacter character) {
        if (characterExistsByName(character.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le nom est déjà utilisé");
        }

        Inventory inventory = inventoryService.createInventory();
        character.setInventoryId(inventory.getId().toString());

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

        return character;
    }

    public Boolean updateCharacterName(int id, String name) throws IllegalArgumentException {
        if(characterExistsByName(name)) throw new IllegalArgumentException("Le nom est déjà utilisé");
        String sql = "UPDATE `character` SET name = ? WHERE idCharacter = ?";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, name);
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();          // <‑‑ executeUpdate ici
            return rows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du changement de nom \""+ name +"\": "+ e.getMessage(), e);
        }
    }

    public void deleteCharacterById(int id) {
        String sql = "DELETE FROM `character` WHERE idCharacter = ?";
        try(Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification du personnage", e);
        }
    }

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


    public boolean characterExistsByName(String name) {
        String sql = "SELECT COUNT(*) FROM `character` WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
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

    public boolean characterExistsById(int id) {
        String sql = "SELECT COUNT(*) FROM `character` WHERE idCharacter = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
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
}