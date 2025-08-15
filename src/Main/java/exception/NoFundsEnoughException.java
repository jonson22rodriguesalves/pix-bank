package exception;

/**
 * Exceção lançada quando uma conta ou carteira não possui saldo suficiente
 * para realizar uma operação financeira solicitada.
 *
 * <p>Esta exceção normalmente ocorre durante operações de saque, transferência
 * ou investimento quando o saldo disponível é menor que o valor necessário.</p>
 */
public class NoFundsEnoughException extends RuntimeException {

    /**
     * Constrói uma nova exceção com a mensagem detalhada especificada.
     *
     * @param message a mensagem detalhada que explica o motivo da exceção.
     *        Normalmente inclui informações sobre o saldo atual e o valor requerido.
     */
    public NoFundsEnoughException(String message) {
        super(message);
    }
}