package be.helha.poo3;

public class ConfigInvalideException extends Exception {

    /** Constructeur par défaut. */
    public ConfigInvalideException() {
        super();
    }

    /**
     * Constructeur avec un message personnalisé.
     * @param message le message détaillé expliquant la raison de l'exception.
     */
    public ConfigInvalideException(String message) {
        super(message);
    }
}
