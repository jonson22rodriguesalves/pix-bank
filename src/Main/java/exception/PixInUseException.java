package exception;

/**
 * Exceção lançada quando uma tentativa de cadastrar uma chave PIX que já está em uso no sistema.
 *
 * <p>Esta exceção indica uma violação da regra de unicidade das chaves PIX,
 * onde cada chave deve ser exclusiva para uma única conta bancária.</p>
 */
public class PixInUseException extends RuntimeException {

    /**
     * Cria uma nova instância da exceção com uma mensagem de erro detalhada.
     *
     * @param message Mensagem descritiva contendo a chave PIX em conflito
     *               e informações adicionais sobre o erro
     */
    public PixInUseException(String message) {
        super(message);
    }
}