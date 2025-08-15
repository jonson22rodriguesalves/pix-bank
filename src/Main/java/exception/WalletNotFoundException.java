package exception;

/**
 * Exceção lançada quando uma carteira de investimento não é encontrada no sistema.
 *
 * <p>Esta exceção é tipicamente usada quando uma operação tenta acessar uma carteira
 * que não existe ou não está disponível para a conta especificada.</p>
 */
public class WalletNotFoundException extends RuntimeException {

    /**
     * Cria uma nova instância da exceção com uma mensagem de erro específica.
     *
     * @param message A mensagem detalhando o motivo da exceção
     */
    public WalletNotFoundException(String message) {
        super(message);
    }
}