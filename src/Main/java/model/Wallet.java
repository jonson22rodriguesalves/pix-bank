package model;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Classe abstrata que representa uma carteira genérica para operações financeiras.
 * Define comportamentos básicos para manipulação de saldo e histórico de transações.
 */
public abstract class Wallet {
    /**
     * Saldo atual da carteira em centavos.
     */
    @Getter
    protected long balance; // saldo em centavos

    /**
     * Histórico de transações financeiras da carteira.
     */
    protected final List<MoneyAudit> transactionHistory = new ArrayList<>();

    /**
     * Tipo de serviço bancário associado à carteira.
     */
    @Getter
    private final BankService service;

    /**
     * Construtor da carteira.
     *
     * @param serviceType Tipo de serviço bancário associado à carteira
     */
    public Wallet(BankService serviceType) {
        this.service = serviceType;
        this.balance = 0;
    }

    /**
     * Adiciona um valor ao saldo da carteira e registra a transação.
     *
     * @param amount Valor a ser adicionado (em centavos)
     * @param description Descrição da operação
     * @throws IllegalArgumentException Se o valor for menor ou igual a zero
     */
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

    /**
     * Reduz o saldo da carteira e registra a transação.
     *
     * @param amount Valor a ser reduzido (em centavos)
     * @param description Descrição da operação
     * @return O valor reduzido
     * @throws IllegalArgumentException Se o valor for inválido ou saldo insuficiente
     */
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

    /**
     * Retorna uma cópia do histórico de transações financeiras.
     *
     * @return Lista de transações financeiras
     */
    public List<MoneyAudit> getFinancialTransactions() {
        return new ArrayList<>(this.transactionHistory);
    }

    /**
     * Retorna o saldo atual da carteira.
     *
     * @return Saldo em centavos
     */
    public long getFunds() {
        return this.balance;
    }

    /**
     * Retorna uma representação em string da carteira.
     *
     * @return String formatada com informações da carteira
     */
    @Override
    public String toString() {
        return "Wallet{" +
                "service=" + service +
                ", balance=R$" + (balance / 100) + "," + String.format("%02d", balance % 100) +
                '}';
    }
}