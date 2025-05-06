package be.helha.poo3.serverpoo.ModelTest;

import be.helha.poo3.serverpoo.models.Monster;
import be.helha.poo3.serverpoo.models.Monsters;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MonsterTest {

    @Test
    void monsterShouldBeAliveWhenCreated() {
        Monster slime = new Monster(Monsters.slime);
        assertTrue(slime.isAlive());
        assertEquals(10, slime.getCurrentHealth());
    }

    @Test
    void monsterShouldTakeDamage() {
        Monster skeleton = new Monster(Monsters.skeleton);
        skeleton.takeDamage(10); // 10 - 4 (defense) = 6 dégats
        assertEquals(9, skeleton.getCurrentHealth()); // 15 - 6 = 9 hp
    }

    @Test
    void monsterShouldNotTakeNegativeDamage() {
        Monster zombie = new Monster(Monsters.zombie);
        zombie.takeDamage(3); // 3 - 4 (defense) = 0 dégats
        assertEquals(20, zombie.getCurrentHealth());
    }

    @Test
    void monsterShouldDieWhenHealthReachesZero() {
        Monster orc = new Monster(Monsters.orc);
        orc.takeDamage(100);
        assertFalse(orc.isAlive());
        assertTrue(orc.getCurrentHealth() <= 0);
    }

    @Test
    void getMonsterNameAndStats() {
        Monster goblin = new Monster(Monsters.goblin);
        assertEquals("Goblin", goblin.getName());
        assertEquals(5, goblin.getDamage());
        assertEquals(Monsters.goblin.getRarity(), goblin.getRarity());
    }
}

