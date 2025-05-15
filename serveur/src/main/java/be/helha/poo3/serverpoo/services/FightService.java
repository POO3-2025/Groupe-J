package be.helha.poo3.serverpoo.services;

import be.helha.poo3.serverpoo.exceptions.InventoryIOException;
import be.helha.poo3.serverpoo.models.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Primary
@Service
public class FightService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ItemLoaderService itemLoaderService;

    @Autowired
    private DungeonMapService dungeonMapService;

    @Autowired
    private InGameCharacterService inGameCharacterService;

    private List<PVMFight> pvmFights = new ArrayList<>();

    //private List<PVPFight> pvpFights = new ArrayList<>();

    public PVMFight getPvmFight(String fightId) {
        ObjectId objectId = new ObjectId(fightId);
        return pvmFights.stream().filter(f -> f.getId().equals(objectId)).findFirst().orElse(null);
    }

    public PVMFight GetPvmFightByCharacterId(int playerId) {
        return pvmFights.stream().filter(f -> f.getPlayer().getId() == playerId).findFirst().orElse(null);
    }

    public PVMFight.PvmTurnResult playPvmTurn(int characterId, String action) {
        PVMFight pvmFight = GetPvmFightByCharacterId(characterId);
        if (pvmFight == null) throw new RuntimeException("Fight not found");
        if (pvmFight.isFinished()) throw new RuntimeException("Fight is finished");
        return pvmFight.turn(action);
    }

    public PVMFightDTO createPvmFight(int characterId) {
        if (GetPvmFightByCharacterId(characterId) != null) throw new RuntimeException("A fight is already running");
        CharacterWithPos character = inGameCharacterService.getCharacterFromGame(characterId);
        if (character == null) throw new RuntimeException("Character not found");
        
        Point characterPosition = character.getPosition();
        Room room = dungeonMapService.getRoomById(characterPosition.x + ":" + characterPosition.y);
        if (room == null) throw new RuntimeException("Room not found");
        if (room.getMonster() == null) throw new RuntimeException("Monster not found");
        
        Inventory inventory = inventoryService.getInventory(new ObjectId(character.getInventoryId()));
        if (inventory == null) throw new RuntimeException("Inventory not found");
        
        Item mainSlot = inventory.getMainSlot();
        Item secondSlot = inventory.getSecondSlot();
        Item armorSlot = inventory.getArmorSlot();
        int damage = mainSlot.extractStatIfExist("damage") + secondSlot.extractStatIfExist("damage") + armorSlot.extractStatIfExist("damage") + secondSlot.extractStatIfExist("power") + armorSlot.extractStatIfExist("power");
        int defense = mainSlot.extractStatIfExist("defense") + secondSlot.extractStatIfExist("defense") + armorSlot.extractStatIfExist("defense");
        int agility = mainSlot.extractStatIfExist("agility") + secondSlot.extractStatIfExist("agility") + armorSlot.extractStatIfExist("agility");

        FightCharacter fightCharacter = new FightCharacter(character.getIdCharacter(), character.getName(), character.getMaxHP(), character.getCurrentHP(), character.getConstitution(), character.getDexterity(), character.getStrength(), damage, defense, agility );
        PVMFight fight = new PVMFight(fightCharacter, room.getMonster());
        pvmFights.add(fight);
        return new PVMFightDTO(fight.getMonster().getCurrentHealth(),fight.getMonster().getType().getHealth(),fight.getPlayer().getCurrentHP(),fight.getPlayer().getMaxHP(),fight.getMonster().getName(),null,fight.getPlayer().getNom(),null);
    }

    public Item getReward(int characterId){
        PVMFight pvmFight = GetPvmFightByCharacterId(characterId);
        if (pvmFight == null) throw new RuntimeException("Fight not found");
        if (pvmFight.isFinished()){
            if (pvmFight.getRewardItem() != null) {
                return pvmFight.getRewardItem();
            } else {
                List<Item> liste = itemLoaderService.findByRarity(pvmFight.getMonster().getRarity());
                if (liste == null || liste.isEmpty()) {
                    return null;
                }
                int randomIndex = ThreadLocalRandom.current().nextInt(liste.size());
                return liste.get(randomIndex);
            }
        }

    }

    public Item endPvmFight(int characterId, boolean get) throws InventoryIOException {
        PVMFight pvmFight = GetPvmFightByCharacterId(characterId);
        if (pvmFight == null) throw new RuntimeException("PVMFight not found");
        if (pvmFight.isFinished()){
            CharacterWithPos character = inGameCharacterService.getCharacterFromGame(characterId);
            if (character == null) throw new RuntimeException("Character not in session");
            Inventory inventory = inventoryService.getInventory(new ObjectId(character.getInventoryId()));
            if (inventory == null) throw new RuntimeException("Inventory not found");
            if (inventory.getItems().size() > 9) throw new InventoryIOException("Too many items", 3);
            Item reward = getReward(characterId);
            pvmFights.remove(pvmFight);
            return reward;
        } else throw new RuntimeException("Fight is not finished");

    }
}
