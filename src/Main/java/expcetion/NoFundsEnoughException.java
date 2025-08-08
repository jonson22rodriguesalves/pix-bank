package expcetion;

public class NoFundsEnoughException extends RuntimeException {

    public NoFundsEnoughException(String message) {
        super(message);
    }

}