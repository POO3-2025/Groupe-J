package be.helha.poo3.serverpoo.exceptions;

public class InventoryIOException extends Exception {
    private final int errorCode;

    public InventoryIOException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
