package model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representa um registro de auditoria financeira para transações monetárias.
 * Contém informações sobre operações financeiras realizadas no sistema.
 *
 * @param transactionId Identificador único da transação
 * @param targetService Serviço bancário de destino da operação
 * @param description Descrição detalhada da transação
 * @param createdAt Data e hora em que a transação foi criada
 */
public record MoneyAudit(
        UUID transactionId,
        BankService targetService,
        String description,
        OffsetDateTime createdAt)
{
    // O record já fornece automaticamente:
    // - Campos final e imutáveis
    // - Construtor padrão
    // - Métodos equals(), hashCode() e toString()
    // - Métodos de acesso para cada componente
}