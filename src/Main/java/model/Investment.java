package model;

/**
 * Representa um tipo de investimento disponível no sistema bancário.
 * Contém informações sobre as características do investimento.
 *
 * @param id Identificador único do investimento
 * @param tax Taxa de rendimento do investimento (em porcentagem)
 * @param initialFunds Valor mínimo inicial para aplicação (em centavos)
 * @param nome Nome/descrição do tipo de investimento
 */
public record Investment(
        long id,
        long tax,
        long initialFunds,
        String nome)
{
    /**
     * Retorna uma representação em string formatada do investimento.
     * Inclui o valor formatado em reais e a taxa de rendimento.
     *
     * @return String formatada com os detalhes do investimento
     */
    @Override
    public String toString() {
        return "Investment{" +
                "id=" + id +
                ", nome=" + nome +
                ", tax=" + tax + "%" +
                ", initialFunds=" + (initialFunds / 100) + "," + (initialFunds % 100) +
                '}';
    }
}