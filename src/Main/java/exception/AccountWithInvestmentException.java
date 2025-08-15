package exception;

/**
 * Exceção lançada quando uma tentativa de operação é realizada em uma conta
 * que já possui um investimento associado.
 * <p>
 * Esta exceção ocorre em situações onde o sistema espera que uma conta não tenha
 * investimentos vinculados, como ao tentar criar um novo investimento para uma
 * conta que já possui um.
 * </p>
 */
public class AccountWithInvestmentException extends RuntimeException {

    /**
     * Constrói uma nova instância da exceção com uma mensagem específica.
     *
     * @param message a mensagem detalhada que descreve a conta e o investimento
     *        existente que causou o conflito
     */
    public AccountWithInvestmentException(String message) {
        super(message);
    }
}