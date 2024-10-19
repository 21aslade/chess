package service;

public class ServiceException extends Exception {
    private final ErrorKind kind;

    public enum ErrorKind {
        AlreadyExists
    }

    public ServiceException(ErrorKind kind) {
        this.kind = kind;
    }

    public ErrorKind kind() {
        return this.kind;
    }
}
