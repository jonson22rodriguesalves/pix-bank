package exception;

/**
 * Exceção lançada quando um tipo de investimento não é encontrado no sistema.
 * <p>
 * Esta exceção ocorre quando uma operação tenta acessar um investimento
 * que não existe ou não está disponível para aplicação.
 * </p>
 */
public class InvestmentNotFoundException extends RuntimeException {

    /**
     * Constrói uma nova exceção com uma mensagem de erro específica.
     *
     * @param message a mensagem detalhada sobre o investimento não encontrado,
     *        normalmente incluindo o ID ou nome do investimento pesquisado
     */
    public InvestmentNotFoundException(String message) {
        super(message);
    }
}