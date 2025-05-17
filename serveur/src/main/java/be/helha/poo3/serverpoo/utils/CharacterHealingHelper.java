package be.helha.poo3.serverpoo.utils;

import be.helha.poo3.serverpoo.models.CharacterWithPos;

public class CharacterHealingHelper {

    /**
     * Soigne le personnage sans dépasser son max HP.
     *
     * @param character personnage à soigner
     * @param healAmount points de soin à appliquer
     * @throws IllegalStateException si les HP sont déjà au maximum
     */
    public static void heal(CharacterWithPos character, int healAmount) {
        int currentHP = character.getCurrentHP();
        int maxHP = character.getMaxHP();

        if (currentHP >= maxHP) {
            throw new IllegalStateException("La barre de vie est déjà pleine.");
        }

        int newHP = Math.min(currentHP + healAmount, maxHP);
        character.setCurrentHP(newHP);
    }
}