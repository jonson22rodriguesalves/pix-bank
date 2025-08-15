package repository;

import exception.NoFundsEnoughException;
import model.Money;
import model.MoneyAudit;
import model.Wallet;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static model.BankService.ACCOUNT;
import static lombok.AccessLevel.PRIVATE;

/**
 * Classe utilitária com métodos comuns para operações financeiras.
 * Contém funções auxiliares reutilizáveis por outros repositórios.
 */
@NoArgsConstructor(access = PRIVATE)
public final class CommonsRepository {

    /**
     * Verifica se há fundos suficientes em uma carteira para uma transação.
     *
     * @param source Carteira de origem dos fundos
     * @param amount Valor a ser verificado (em centavos)
     * @throws NoFundsEnoughException Se o saldo for insuficiente para a transação
     */
    public static void checkFundsForTransaction(final Wallet source, final long amount) {
        if (source.getFunds() < amount) {
            throw new NoFundsEnoughException("Sua conta nao tem dinheiro o suficiente para realizar essa transacao");
        }
    }

    /**
     * Gera uma lista de objetos Money para representar uma quantia em dinheiro.
     * Cada unidade de Money é associada ao mesmo histórico de transação.
     *
     * @param transactionId ID único da transação
     * @param funds Quantidade de unidades monetárias a serem geradas
     * @param description Descrição da transação
     * @return Lista de objetos Money representando o valor total
     */
    public static List<Money> generateMoney(final UUID transactionId, final long funds, final String description) {
        var history = new MoneyAudit(transactionId, ACCOUNT, description, OffsetDateTime.now());
        return Stream.generate(() -> new Money(history)).limit(funds).toList();
    }
}