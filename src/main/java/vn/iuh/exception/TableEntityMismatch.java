package vn.iuh.exception;

public class TableEntityMismatch extends RuntimeException {
    public TableEntityMismatch(String message) {
        super(message);
    }
}
