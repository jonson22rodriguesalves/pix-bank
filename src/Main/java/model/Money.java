package model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa uma unidade monetária com histórico de transações.
 * Cada instância mantém um rastreamento completo das operações financeiras realizadas.
 */
@EqualsAndHashCode
@ToString
@Getter
public class Money {

    /**
     * Lista de registros de auditoria que compõem o histórico financeiro desta unidade monetária.
     */
    private final List<MoneyAudit> history = new ArrayList<>();

    /**
     * Cria uma nova unidade monetária com um registro inicial de histórico.
     *
     * @param history O registro de auditoria inicial associado a este dinheiro
     */
    public Money(final MoneyAudit history) {
        this.history.add(history);
    }

    /**
     * Adiciona um novo registro ao histórico desta unidade monetária.
     *
     * @param history O registro de auditoria a ser adicionado ao histórico
     */
    public void addHistory(final MoneyAudit history) {
        this.history.add(history);
    }
}