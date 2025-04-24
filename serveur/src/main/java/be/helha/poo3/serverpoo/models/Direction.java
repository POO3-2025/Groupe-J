package be.helha.poo3.serverpoo.models;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    //permet de récupérer la direction opposée (utile pour la connexion de salles)
    public Direction opposée() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
        }
        throw new IllegalStateException("Direction inconnue");
    }
}

