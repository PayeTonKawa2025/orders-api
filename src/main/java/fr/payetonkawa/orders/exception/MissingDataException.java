package fr.payetonkawa.orders.exception;

public class MissingDataException extends RuntimeException {
    public MissingDataException(String message) {
        super(message);
    }
}
