package model;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static model.BankService.INVESTMENT;

@Getter
public class InvestmentWallet extends Wallet {

    private final Investment investment;
    private final AccountWallet account;
    private long balance; // saldo em centavos
    private final List<MoneyAudit> transactionHistory = new ArrayList<>();

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

    public void addMoney(long amount, String description) {
        this.balance += amount;
        this.transactionHistory.add(new MoneyAudit(
                UUID.randomUUID(),
                INVESTMENT,
                description,
                OffsetDateTime.now()
        ));
    }

    public long reduceMoney(long amount) {
        this.balance -= amount;
        return amount;
    }

    public long getFunds() {
        return this.balance;
    }

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

    public List<MoneyAudit> getFinancialTransactions() {
        return new ArrayList<>(this.transactionHistory);
    }

    @Override
    public String toString() {
        return "InvestmentWallet{" +
                "investment=" + investment +
                ", account=" + account +
                ", balance=" + (balance / 100) + "," + (balance % 100) +
                '}';
    }
}