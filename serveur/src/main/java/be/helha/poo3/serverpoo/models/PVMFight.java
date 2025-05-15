package be.helha.poo3.serverpoo.models;

import org.bson.types.ObjectId;

import java.util.concurrent.ThreadLocalRandom;

public class PVMFight {
    ObjectId id;
    FightCharacter player;
    Monster monster;
    int monsterCurrentHealth;
    String turn;

    public PVMFight(FightCharacter player, Monster monster) {
        this.id = new ObjectId();
        this.player = player;
        this.monster = monster;
        this.monsterCurrentHealth = monster.getType().getHealth();
    }

    public TurnResult turn (String playerAction) {
        String monsterAction = getMonsterAction();
        if ((playerAction.equals("dodge") || playerAction.equals("block")) &&
                monsterAction.equals("block")) {
            return new TurnResult(false, 0, 0, playerAction, monsterAction);
        }

        int damageMonsterTake = 0;
        int damagePlayerTake = 0;

        if (playerAction.equals("attack")) {
            int playerAttack = player.getDamage();
            int monsterDefense = monsterAction.equals("block")? monster.getType().getDefense() *2 : monster.getType().getDefense();

            damageMonsterTake = calculateDamage((int) (playerAttack * (1+(player.getStrength()*0.03))), monsterDefense);
            monsterCurrentHealth = Math.max(0, monsterCurrentHealth - damageMonsterTake);
        }

        if (monsterAction.equals("attack")) {
            int monsterAttack = monster.getType().getDamage();
            int playerDefense = player.getDefense();
            boolean dodged = false;

            if (playerAction.equals("dodge")) {
                double baseChance = 0.50;
                double agilityFactor = ((player.getAgility() / 2.0) + 100.0) / 100.0;
                double dexterityBonus = 0.03 * player.getDexterity();
                double dodgeChance = baseChance * agilityFactor + dexterityBonus;
                dodged = Math.random() < dodgeChance;
            }

            else if (playerAction.equals("block")) {
                playerDefense *= 2;
            }

            if (!dodged) {
                damagePlayerTake = calculateDamage(monsterAttack, playerDefense);
                int newHp        = Math.max(0, player.getCurrentHP() - damagePlayerTake);
                player.setCurrentHP(newHp);
            }
        }
        boolean fightEnd = player.getCurrentHP() <= 0 || monsterCurrentHealth <= 0;

        return new TurnResult(fightEnd, damageMonsterTake, damagePlayerTake, playerAction, monsterAction);


    }

    private String getMonsterAction(){
        double playerHealthPercentage = (double) player.getCurrentHP() / player.getMaxHP();
        if(playerHealthPercentage < 30) {
            return "attack";
        }
        double monsterHealthPercentage = (double) monsterCurrentHealth /monster.getType().getHealth()*100;

        Rarity monsterRarity = monster.getType().getRarity();
        switch (monsterRarity) {
            case common:
                return monsterHealthPercentage > 50 ? "attack" : "block";
            case uncommon, rare:
                return monsterHealthPercentage > 25 ? "attack" : "block";
            default:
                return monsterHealthPercentage > 15 ? "attack" : "block";
        }
    }

    public static int calculateDamage(int damage, int defense) {
        return ((damage + 100)/(defense + 100) * 25)+ ThreadLocalRandom.current().nextInt(-2, 3);
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public FightCharacter getPlayer() {
        return player;
    }

    public void setPlayer(FightCharacter player) {
        this.player = player;
    }

    public Monster getMonster() {
        return monster;
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    public int getMonsterCurrentHealth() {
        return monsterCurrentHealth;
    }

    public void setMonsterCurrentHealth(int monsterCurrentHealth) {
        this.monsterCurrentHealth = monsterCurrentHealth;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public class TurnResult {
        private boolean fightEnd;
        private int damageMonsterTake;
        private int damagePlayerTake;
        private String playerAction;
        private String monsterAction;

        public TurnResult(boolean fightEnd, int damageMonsterTake, int damagePlayerTake, String playerAction, String monsterAction) {
            this.fightEnd = fightEnd;
            this.damageMonsterTake = damageMonsterTake;
            this.damagePlayerTake = damagePlayerTake;
            this.playerAction = playerAction;
            this.monsterAction = monsterAction;
        }

        public boolean isFightEnd() {
            return fightEnd;
        }

        public void setFightEnd(boolean fightEnd) {
            this.fightEnd = fightEnd;
        }

        public int getDamageMonsterTake() {
            return damageMonsterTake;
        }

        public void setDamageMonsterTake(int damageMonsterTake) {
            this.damageMonsterTake = damageMonsterTake;
        }

        public int getDamagePlayerTake() {
            return damagePlayerTake;
        }

        public void setDamagePlayerTake(int damagePlayerTake) {
            this.damagePlayerTake = damagePlayerTake;
        }

        public String getPlayerAction() {
            return playerAction;
        }

        public void setPlayerAction(String playerAction) {
            this.playerAction = playerAction;
        }

        public String getMonsterAction() {
            return monsterAction;
        }

        public void setMonsterAction(String monsterAction) {
            this.monsterAction = monsterAction;
        }
    }
}

