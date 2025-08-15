package model;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static model.BankService.INVESTMENT;

/**
 * Representa uma carteira de investimento associada a uma conta bancária.
 * Gerencia operações específicas de investimentos como aplicações, resgates e rendimentos.
 */
@Getter
public class InvestmentWallet extends Wallet {

    /**
     * Tipo de investimento associado a esta carteira.
     */
    private final Investment investment;

    /**
     * Conta bancária vinculada a este investimento.
     */
    private final AccountWallet account;

    /**
     * Saldo atual do investimento em centavos.
     */
    private long balance; // saldo em centavos

    /**
     * Histórico de transações específicas deste investimento.
     */
    private final List<MoneyAudit> transactionHistory = new ArrayList<>();

    /**
     * Cria uma nova carteira de investimento vinculada a uma conta.
     *
     * @param investment Tipo de investimento
     * @param account Conta bancária associada
     * @param amount Valor inicial do investimento (em centavos)
     */
    public InvestmentWallet(final Investment investment, final AccountWallet account, final long amount) {
        super(INVESTMENT);
        this.investment = investment;
        this.account = account;

        // Descrição para a retirada da conta
        String withdrawalDescription = "Aplicação inicial em investimento " + investment.id() + " " +
                " no valor de R$" + (investment.initialFunds() / 100) +
                "," + String.format("%02d", investment.initialFunds() % 100);

        // Remove o valor da conta com registro
        this.balance = account.reduceMoney(amount, withdrawalDescription);

        // Registra a criação do investimento
        this.transactionHistory.add(new MoneyAudit(
                UUID.randomUUID(),
                INVESTMENT,
                "Investimento inicial: R$" + (amount/100) + "," + String.format("%02d", amount%100),
                OffsetDateTime.now()
        ));

        // Registra na conta o investimento realizado
        account.getFinancialTransactions().add(new MoneyAudit(
                UUID.randomUUID(),
                INVESTMENT,
                "Aplicação em " + investment.id(),
                OffsetDateTime.now()
        ));
    }

    /**
     * Adiciona um valor ao saldo do investimento e registra a transação.
     *
     * @param amount Valor a ser adicionado (em centavos)
     * @param description Descrição da operação
     */
    public void addMoney(long amount, String description) {
        this.balance += amount;
        this.transactionHistory.add(new MoneyAudit(
                UUID.randomUUID(),
                INVESTMENT,
                description,
                OffsetDateTime.now()
        ));
    }

    /**
     * Reduz o saldo do investimento sem registro de transação.
     *
     * @param amount Valor a ser reduzido (em centavos)
     * @return O valor reduzido
     */
    public long reduceMoney(long amount) {
        this.balance -= amount;
        return amount;
    }

    /**
     * Retorna o saldo atual do investimento.
     *
     * @return Saldo em centavos
     */
    public long getFunds() {
        return this.balance;
    }

    /**
     * Atualiza o valor do investimento aplicando a porcentagem de rendimento.
     *
     * @param percent Porcentagem de rendimento a ser aplicada
     */
    public void updateAmount(final long percent) {
        long earnings = this.balance * percent / 100;
        this.balance += earnings;
        this.transactionHistory.add(new MoneyAudit(
                UUID.randomUUID(),
                INVESTMENT,
                "rendimentos (" + percent + "%)",
                OffsetDateTime.now()
        ));
    }

    /**
     * Retorna uma cópia do histórico de transações do investimento.
     *
     * @return Lista de transações financeiras
     */
    public List<MoneyAudit> getFinancialTransactions() {
        return new ArrayList<>(this.transactionHistory);
    }

    /**
     * Retorna uma representação em string da carteira de investimento.
     *
     * @return String formatada com informações do investimento
     */
    @Override
    public String toString() {
        return "InvestmentWallet{" +
                "investment=" + investment +
                ", account=" + account +
                ", balance=" + (balance / 100) + "," + (balance % 100) +
                '}';
    }
}