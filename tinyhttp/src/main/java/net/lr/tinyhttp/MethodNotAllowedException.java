package net.lr.tinyhttp;

public class MethodNotAllowedException extends RuntimeException {

    private static final long serialVersionUID = -3380780688359281558L;

    public MethodNotAllowedException(String method) {
        super("Method " + method + " is not allowed for this ressource");
    }

}
