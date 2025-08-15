package exception;

/**
 * Exceção lançada quando uma conta bancária não é encontrada no sistema.
 * <p>
 * Esta exceção é geralmente lançada quando uma operação tenta acessar uma conta
 * que não existe, foi removida ou está inativa no sistema.
 * </p>
 */
public class AccountNotFoundException extends RuntimeException {

    /**
     * Constrói uma nova instância da exceção com uma mensagem de erro específica.
     *
     * @param message a mensagem detalhada contendo informações sobre a conta não encontrada,
     *        normalmente incluindo o identificador ou chave PIX utilizada na busca
     */
    public AccountNotFoundException(String message) {
        super(message);
    }
}