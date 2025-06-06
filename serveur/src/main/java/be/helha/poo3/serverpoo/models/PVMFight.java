package be.helha.poo3.serverpoo.models;

import org.bson.types.ObjectId;

import java.util.concurrent.ThreadLocalRandom;

public class PVMFight {
    ObjectId id;
    FightCharacter player;
    Monster monster;
    String turn;
    boolean finished;
    Item rewardItem;

    public PVMFight(FightCharacter player, Monster monster) {
        this.id = new ObjectId();
        this.player = player;
        this.monster = monster;
        this.finished = false;
        this.rewardItem = null;
    }

    public PvmTurnResult turn (String playerAction) {
        String monsterAction = getMonsterAction();
        if ((playerAction.equals("dodge") || playerAction.equals("block")) &&
                monsterAction.equals("block")) {
            return new PvmTurnResult(false, this.player.getNom(), this.monster.getName() ,this.player.getCurrentHP(), this.monster.getCurrentHealth(), this.player.getMaxHP(), this.monster.getType().getHealth(),0,0, playerAction, monsterAction);
        }

        int damageMonsterTake = 0;
        int damagePlayerTake = 0;

        if (playerAction.equals("attack")) {
            int playerAttack = player.getDamage();
            int monsterDefense = monsterAction.equals("block")? monster.getType().getDefense() *2 : monster.getType().getDefense();

            damageMonsterTake = Math.max(0, calculateDamage((int) (playerAttack * (1+(player.getStrength()*0.03))), monsterDefense));
            monster.setCurrentHealth(Math.max(0, monster.getCurrentHealth() - damageMonsterTake));
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
                damagePlayerTake = Math.max(0, calculateDamage(monsterAttack, playerDefense));
                int newHp        = Math.max(0, player.getCurrentHP() - damagePlayerTake);
                player.setCurrentHP(newHp);
            }
        }
        boolean fightEnd = player.getCurrentHP() <= 0 || monster.getCurrentHealth() <= 0;

        setFinished(fightEnd);

        return new PvmTurnResult(fightEnd, this.player.getNom(), this.monster.getName(), this.player.getCurrentHP(), this.monster.getCurrentHealth(), this.player.getMaxHP(), this.monster.getType().getHealth(), damageMonsterTake, damagePlayerTake, playerAction, monsterAction);


    }

    private String getMonsterAction(){
        double playerHealthPercentage = (double) player.getCurrentHP() / player.getMaxHP();
        if(playerHealthPercentage < 30) {
            return "attack";
        }
        double monsterHealthPercentage = (double) monster.getCurrentHealth() /monster.getType().getHealth()*100;

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

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Item getRewardItem() {
        return rewardItem;
    }

    public void setRewardItem(Item rewardItem) {
        this.rewardItem = rewardItem;
    }

    public static class PvmTurnResult {
        private boolean fightEnd;
        private String player;
        private String monster;
        private int playerHealth;
        private int monsterHealth;
        private int playerMaxHealth;
        private int monsterMaxHealth;
        private int damageMonsterTake;
        private int damagePlayerTake;
        private String playerAction;
        private String monsterAction;

        public PvmTurnResult(boolean fightEnd, String player, String monster, int playerHealth, int monsterHealth, int playerMaxHealth, int monsterMaxHealth, int damageMonsterTake, int damagePlayerTake, String playerAction, String monsterAction) {
            this.fightEnd = fightEnd;
            this.player = player;
            this.monster = monster;
            this.playerHealth = playerHealth;
            this.monsterHealth = monsterHealth;
            this.playerMaxHealth = playerMaxHealth;
            this.monsterMaxHealth = monsterMaxHealth;
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

        public String getPlayer() {
            return player;
        }

        public void setPlayer(String player) {
            this.player = player;
        }

        public String getMonster() {
            return monster;
        }

        public void setMonster(String monster) {
            this.monster = monster;
        }

        public int getPlayerHealth() {
            return playerHealth;
        }

        public void setPlayerHealth(int playerHealth) {
            this.playerHealth = playerHealth;
        }

        public int getMonsterHealth() {
            return monsterHealth;
        }

        public void setMonsterHealth(int monsterHealth) {
            this.monsterHealth = monsterHealth;
        }

        public int getPlayerMaxHealth() {
            return playerMaxHealth;
        }

        public void setPlayerMaxHealth(int playerMaxHealth) {
            this.playerMaxHealth = playerMaxHealth;
        }

        public int getMonsterMaxHealth() {
            return monsterMaxHealth;
        }

        public void setMonsterMaxHealth(int monsterMaxHealth) {
            this.monsterMaxHealth = monsterMaxHealth;
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

