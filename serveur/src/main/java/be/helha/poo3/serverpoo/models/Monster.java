package be.helha.poo3.serverpoo.models;

public class Monster {
    private Monsters type;
    private int currentHealth;
    private Temperament temperament;

    public Monster(Monsters type) {
        this.type = type;
        this.currentHealth = type.getHealth();
        this.temperament = type.getTemperament();
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public void takeDamage(int amount) {
        int damageTaken = Math.max(0, amount - type.getDefense());
        currentHealth -= damageTaken;
    }

    public Monsters getType(){ return type; }

    public double getHealthPercentage() {
        return (double) currentHealth / type.getHealth() *100;
    }

    public String getName() {
        return type.getName();
    }

    public int getDamage() {
        return type.getDamage();
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public Rarity getRarity() {
        return type.getRarity();
    }

    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }

    public Temperament getTemperament() { return temperament; }
}

