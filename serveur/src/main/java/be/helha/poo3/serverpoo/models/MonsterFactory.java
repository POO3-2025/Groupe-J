package be.helha.poo3.serverpoo.models;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MonsterFactory {

    private static final Random random = new Random();

    public static Monster generateRandomMonster() {
        Monsters[] types = Monsters.values();
        Monsters randomType = types[random.nextInt(types.length)];
        return new Monster(randomType);
    }

    public static Monster generateMonsterByRarity(Rarity rarity) {
        List<Monsters> filtered = Arrays.stream(Monsters.values())
                .filter(type -> type.getRarity() == rarity)
                .toList();

        if (filtered.isEmpty()) return null;

        return new Monster(filtered.get(random.nextInt(filtered.size())));
    }
}

