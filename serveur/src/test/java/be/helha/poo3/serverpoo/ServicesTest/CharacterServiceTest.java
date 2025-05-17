package be.helha.poo3.serverpoo.ServicesTest;

import be.helha.poo3.serverpoo.config.TestMongoConfig;
import be.helha.poo3.serverpoo.models.GameCharacter;
import be.helha.poo3.serverpoo.services.CharacterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests d'intégration/unitaires pour {@link CharacterService}.
 * <p>
 * - Le contexte Mongo est démarré via {@link TestMongoConfig} comme pour les tests d'ItemLoaderService ;
 * - La partie SQL est simulée grâce à Mockito (DataSource, Connection, PreparedStatement, ResultSet) de la même
 *   manière que dans {@code UserServiceTest}.
 * <p>
 * ⚠️  Depuis Spring Boot 3.4, {@code @MockBean} et {@code @SpyBean} sont dépréciés ; on utilise donc ici
 *     {@code @MockitoBean} et {@code @MockitoSpyBean} provenant du Spring Framework 6.2.
 */
@SpringBootTest(classes = TestMongoConfig.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CharacterServiceTest {

    @MockitoSpyBean
    private CharacterService characterService;

    @MockitoBean
    private DataSource dataSource; // remplace la vraie source MySQL

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // Tous les appels à dataSource.getConnection() renvoient désormais la connexion mockée
        when(dataSource.getConnection()).thenReturn(mockConnection);
    }

    /**
     * Vérifie que {@link CharacterService#getCharactersByUser(int)} reconstruit correctement les personnages
     * à partir du ResultSet renvoyé par la base SQL.
     */
    @Test
    void shouldReturnCharactersForGivenUser() throws Exception {
        int userId = 1;

        // Préparation de la requête
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Simule un seul enregistrement, puis fin du ResultSet
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("idCharacter")).thenReturn(10);
        when(mockResultSet.getInt("idUser")).thenReturn(userId);
        when(mockResultSet.getString("name")).thenReturn("Hero");
        when(mockResultSet.getString("inventoryId")).thenReturn("dummyInv");
        when(mockResultSet.getInt("maxHP")).thenReturn(100);
        when(mockResultSet.getInt("currentHP")).thenReturn(100);
        when(mockResultSet.getInt("constitution")).thenReturn(10);
        when(mockResultSet.getInt("dexterity")).thenReturn(12);
        when(mockResultSet.getInt("strength")).thenReturn(15);

        // Appel SUT
        List<GameCharacter> characters = characterService.getCharactersByUser(userId);

        // Vérifications
        assertEquals(1, characters.size(), "Il doit y avoir exactement un personnage");
        GameCharacter gc = characters.get(0);
        assertEquals("Hero", gc.getName());
        assertEquals(userId, gc.getIdUser());
        assertEquals(100, gc.getMaxHP());
    }

    /**
     * Vérifie que la méthode {@link CharacterService#updateCharacterName(int, String)} renvoie bien {@code true}
     * lorsque le nom est disponible et que l'UPDATE impacte au moins une ligne.
     */
    @Test
    void shouldUpdateCharacterName() throws Exception {
        int characterId = 10;
        String newName = "NewHero";

        // On court‑circuite le test d'existence du nom pour se concentrer sur l'UPDATE
        Mockito.doReturn(false).when(characterService).characterExistsByName(newName);

        // Préparation de la requête UPDATE
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Simule une ligne mise à jour

        boolean result = characterService.updateCharacterName(characterId, newName);
        assertTrue(result, "La mise à jour du nom doit renvoyer true si au moins une ligne est impactée");
    }

    /**
     * Vérifie le scénario où l'utilisateur possède bien le personnage.
     */
    @Test
    void shouldReturnTrueWhenUserOwnsCharacter() throws Exception {
        int userId = 1;
        int characterId = 10;

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);  // Un enregistrement trouvé
        when(mockResultSet.getInt(1)).thenReturn(1); // COUNT(*) = 1

        assertTrue(characterService.userOwnsCharacter(userId, characterId));
    }

    /**
     * Exemple de test où le nom de personnage est déjà utilisé et doit lever IllegalArgumentException.
     */
    @Test
    void shouldThrowWhenNameAlreadyExists() throws Exception {
        String usedName = "UsedName";
        Mockito.doReturn(true).when(characterService).characterExistsByName(usedName);

        assertThrows(IllegalArgumentException.class,
                () -> characterService.updateCharacterName(1, usedName));
    }
}
