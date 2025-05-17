package be.helha.poo3.serverpoo.services;


import be.helha.poo3.serverpoo.models.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Primary
@Service
public class PVPFightService {
    @Autowired
    private InGameCharacterService inGameCharacterService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private FightService fightService;

    private final Map<ObjectId, ChallengeRequest> challengeRequests = new ConcurrentHashMap<>();
    private final Map<ObjectId, PVPFight> fights = new ConcurrentHashMap<>();

    public ChallengeRequest createChallenge(int challengerId, int targetId) {
        ensurePlayerFree(challengerId);
        ensurePlayerFree(targetId);

        FightCharacter challenger = createFightCharacter(challengerId);
        FightCharacter target = createFightCharacter(targetId);

        ChallengeRequest challengeRequest = new ChallengeRequest(challenger, target);
        challengeRequests.put(challengeRequest.getId(), challengeRequest);
        return challengeRequest;
    }

    public ChallengeRequest acceptChallenge(ObjectId challengeId, int targetId) {
        ChallengeRequest challengeRequest = getChallenge(challengeId);
        if (challengeRequest.getStatus() != ChallengeRequest.ChallengeStatus.PENDING
                || challengeRequest.getTarget().getId() != targetId)
            throw new RuntimeException("Can't accept challenge with id " + challengeId);
        challengeRequest.setStatus(ChallengeRequest.ChallengeStatus.ACCEPTED);
        PVPFight fight = new PVPFight(challengeRequest.getChallenger(),challengeRequest.getTarget());
        fights.put(fight.getId(), fight);
        return challengeRequest;
    }

    public void cancelChallenge(ObjectId challengeId, int playerId) {
        ChallengeRequest challengeRequest = getChallenge(challengeId);
        if (challengeRequest.getStatus() == ChallengeRequest.ChallengeStatus.DECLINED)return;
        if (playerId != challengeRequest.getTarget().getId() && playerId != challengeRequest.getChallenger().getId())
            throw new RuntimeException("Not your challenge");

        challengeRequest.setStatus(ChallengeRequest.ChallengeStatus.DECLINED);
    }

    public ChallengeRequest getChallengeToMe(int playerId) {
        return challengeRequests.values()
                .stream()
                .filter(challengeRequest -> challengeRequest.getTarget().getId() == playerId)
                .findFirst().orElse(null);
    }

    public ChallengeRequest getChallengeFromMe(int playerId) {
        return challengeRequests.values()
                .stream()
                .filter(challengeRequest -> challengeRequest.getChallenger().getId() == playerId)
                .findFirst().orElse(null);
    }

    public ChallengeRequest getChallenge(ObjectId id) {
        ChallengeRequest challengeRequest = challengeRequests.get(id);
        if (challengeRequest == null) throw new RuntimeException("Challenge not found");
        return challengeRequest;
    }

    public PVPFightDTO submitAction(int playerId, String action) {

        PVPFight fight = getFightByPlayerId(playerId);
        if (fight == null) throw new RuntimeException("Can't find fight for player with id " + playerId);

        fight.submitAction(playerId,action,(playerOneAction, playerTwoAction)-> resolvePvpTurn(fight,playerOneAction,playerTwoAction));

        PVPFight.PVPTurn turn = fight.getTurns().get(fight.getTurn().get() - 1);
        if (turn == null) return null;
        else {
            PVPFightDTO result = turn.getResult();
            result.setPlayerID(playerId);
            return result;
        }
    }

    public PVPFight getFightByPlayerId(int playerId) {
        return fights.values().stream()
                .filter(f ->
                        (f.getPlayerOne().getId() == playerId
                                || f.getPlayerTwo().getId() == playerId) && !f.isFinished())
                .findFirst().orElse(null);
    }

    public PVPFightDTO resolvePvpTurn(PVPFight fight, String playerOneAction, String playerTwoAction) {
        playerOneAction = playerOneAction.toLowerCase();
        playerTwoAction = playerTwoAction.toLowerCase();

        FightCharacter playerOne = fight.getPlayerOne();
        FightCharacter playerTwo = fight.getPlayerTwo();

        if(playerOneAction.equals("forfeit") || playerTwoAction.equals("forfeit")){
            fight.setFinished(true);
            return new PVPFightDTO(
                    true,
                    fight.getId().toHexString(),
                    fight.getTurn().get(),
                    0,
                    playerOne.getId(),
                    playerOne.getNom(),
                    playerOne.getCurrentHP(),
                    playerOne.getMaxHP(),
                    playerOneAction,
                    0,
                    playerTwo.getId(),
                    playerTwo.getNom(),
                    playerTwo.getCurrentHP(),
                    playerTwo.getMaxHP(),
                    playerTwoAction,
                    0
            );
        }
        int playerOneDamageTaken = 0;
        int playerTwoDamageTaken = 0;

        if(playerOneAction.equals("attack")){
            int playerOneAttack = playerOne.getDamage();
            int playerTwoDefense = playerTwo.getDefense();

            if(playerTwoAction.equals("block")) playerTwoDefense *=2;

            boolean dodge = false;

            if(playerTwoAction.equals("dodge")){
                double base = 0.50;
                double agilityFactor = ((playerTwo.getAgility() /2.0) +100.0)/100.0;
                double dexterityBonus = 0.03 * playerTwo.getDexterity();
                double dodgeChance = base * agilityFactor + dexterityBonus;
                dodge = ThreadLocalRandom.current().nextDouble() < dodgeChance;
            }

            if (!dodge){
                playerTwoDamageTaken = PVMFight.calculateDamage((int) (playerOneAttack * (1+(playerOne.getStrength()*0.03))), playerTwoDefense);
                playerTwoDamageTaken = (int) (playerTwoDamageTaken * (1.0-(playerTwo.getConstitution()*0.03)));
            }
        }

        if(playerTwoAction.equals("attack")){
            int playerTwoAttack = playerTwo.getDamage();
            int playerOneDefense = playerOne.getDefense();

            if(playerOneAction.equals("block")) playerOneDefense *=2;

            boolean dodge = false;

            if(playerOneAction.equals("dodge")){
                double base = 0.50;
                double agilityFactor = ((playerOne.getAgility() /2.0) +100.0)/100.0;
                double dexterityBonus = 0.03 * playerOne.getDexterity();
                double dodgeChance = base * agilityFactor + dexterityBonus;
                dodge = ThreadLocalRandom.current().nextDouble() < dodgeChance;
            }

            if (!dodge){
                playerOneDamageTaken = PVMFight.calculateDamage((int) (playerTwoAttack * (1+(playerTwo.getStrength()*0.03))), playerOneDefense);
                playerOneDamageTaken = (int) (playerOneDamageTaken * (1.0-(playerOne.getConstitution()*0.03)));
            }
        }

        boolean end = playerOne.getCurrentHP() <= 0 || playerTwo.getCurrentHP() <= 0;
        if (end) fight.setFinished(true);

        return new PVPFightDTO(
                true,
                fight.getId().toHexString(),
                fight.getTurn().get(),
                0,
                playerOne.getId(),
                playerOne.getNom(),
                playerOne.getCurrentHP(),
                playerOne.getMaxHP(),
                playerOneAction,
                playerOneDamageTaken,
                playerTwo.getId(),
                playerTwo.getNom(),
                playerTwo.getCurrentHP(),
                playerTwo.getMaxHP(),
                playerTwoAction,
                playerTwoDamageTaken
        );
    }


    public PVPFight getFight(ObjectId id) {
        PVPFight fight = fights.get(id);
        if (fight == null) throw new RuntimeException("Fight not found");
        return fight;
    }

    private FightCharacter createFightCharacter(int id) {
        CharacterWithPos character = inGameCharacterService.getCharacterFromGame(id);
        Inventory inventory = inventoryService.getInventory(new ObjectId(character.getInventoryId()));
        if (inventory == null) throw new RuntimeException("Inventory not found");

        Item mainSlot = inventory.getMainSlot();
        Item secondSlot = inventory.getSecondSlot();
        Item armorSlot = inventory.getArmorSlot();
        int damage = 0;
        int defense = 0;
        int agility = 0;
        if (mainSlot != null){
            damage += mainSlot.extractStatIfExist("damage") + mainSlot.extractStatIfExist("power");
            defense += mainSlot.extractStatIfExist("defense");
            agility += mainSlot.extractStatIfExist("agility");
        }
        if (secondSlot != null){
            damage += secondSlot.extractStatIfExist("damage") + secondSlot.extractStatIfExist("power");
            defense += secondSlot.extractStatIfExist("defense");
            agility += secondSlot.extractStatIfExist("agility");
        }
        if (armorSlot != null){
            damage += armorSlot.extractStatIfExist("damage") + armorSlot.extractStatIfExist("power");
            defense += armorSlot.extractStatIfExist("defense");
            agility += armorSlot.extractStatIfExist("agility");
        }
        return new FightCharacter(character.getIdCharacter(), character.getName(), character.getMaxHP(), character.getCurrentHP(), character.getConstitution(), character.getDexterity(), character.getStrength(), damage, defense, agility );
    }

    private void ensurePlayerFree(int id) {
        boolean busy = challengeRequests.values().stream()
                .anyMatch(challenge -> challenge.getStatus()==ChallengeRequest.ChallengeStatus.PENDING &&
                        (challenge.getChallenger().getId()==id || challenge.getTarget().getId()==id))
                || fights.values().stream()
                .anyMatch(fight -> !fight.isFinished() &&
                        (fight.getPlayerOne().getId()==id || fight.getPlayerTwo().getId()==id));
        if (busy) throw new RuntimeException("Player busy");
    }

}
