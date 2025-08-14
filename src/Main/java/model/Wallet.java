package model;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Wallet {
    @Getter
    protected long balance; // saldo em centavos
    protected final List<MoneyAudit> transactionHistory = new ArrayList<>();
    @Getter
    private final BankService service;

    public Wallet(BankService serviceType) {
        this.service = serviceType;
        this.balance = 0;
    }

    public void addMoney(long amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
        this.balance += amount;
        this.transactionHistory.add(new MoneyAudit(
                UUID.randomUUID(),
                this.service,
                description,
                OffsetDateTime.now()
        ));
    }

    public long reduceMoney(long amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Valor deve ser positivo");
        }
        if (amount > this.balance) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }
        this.balance -= amount;

        // Registra a transação no histórico
        this.transactionHistory.add(new MoneyAudit(
                UUID.randomUUID(),
                this.service,
                description,
                OffsetDateTime.now()
        ));

        return amount;
    }

    public List<MoneyAudit> getFinancialTransactions() {
        return new ArrayList<>(this.transactionHistory);
    }

    public long getFunds() {
        return this.balance;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "service=" + service +
                ", balance=R$" + (balance / 100) + "," + String.format("%02d", balance % 100) +
                '}';
    }
}