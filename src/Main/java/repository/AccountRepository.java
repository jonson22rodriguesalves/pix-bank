package repository;

import exception.AccountNotFoundException;
import exception.PixInUseException;
import model.AccountWallet;
import model.MoneyAudit;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static repository.CommonsRepository.checkFundsForTransaction;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Repositório responsável pela gestão de contas bancárias.
 * Gerencia operações como criação de contas, depósitos, saques e transferências PIX.
 */
public class AccountRepository {

    /**
     * Lista de contas bancárias cadastradas no sistema.
     */
    private final List<AccountWallet> accounts = new ArrayList<>();

    /**
     * Cria uma nova conta bancária com chaves PIX.
     *
     * @param pix Lista de chaves PIX associadas à conta
     * @param initialFunds Valor inicial do depósito (em centavos)
     * @param depositDescription Descrição do depósito inicial
     * @return A conta criada
     * @throws PixInUseException Se alguma chave PIX já estiver em uso
     */
    public AccountWallet create(final List<String> pix, final long initialFunds, final String depositDescription) {
        if (!accounts.isEmpty()) {
            var pixInUse = accounts.stream().flatMap(a -> a.getPix().stream()).toList();
            for (var p : pix) {
                if (pixInUse.contains(p)) {
                    throw new PixInUseException("O pix '" + p + "' já está em uso");
                }
            }
        }
        var newAccount = new AccountWallet(initialFunds, pix, depositDescription);
        accounts.add(newAccount);
        return newAccount;
    }

    /**
     * Realiza um depósito em uma conta existente.
     *
     * @param pix Chave PIX da conta de destino
     * @param fundsAmount Valor do depósito (em centavos)
     * @param depositDescription Descrição do depósito
     * @throws AccountNotFoundException Se a conta não for encontrada
     */
    public void deposit(final String pix, final long fundsAmount, final String depositDescription) {
        var target = findByPix(pix);
        target.addMoney(fundsAmount, depositDescription);
    }

    /**
     * Realiza um saque de uma conta existente.
     *
     * @param pix Chave PIX da conta
     * @param amount Valor do saque (em centavos)
     * @return O valor sacado
     * @throws AccountNotFoundException Se a conta não for encontrada
     * @throws -NoFundsEnoughException- Se o saldo for insuficiente
     */
    public long withdraw(final String pix, final long amount) {
        var source = findByPix(pix);
        checkFundsForTransaction(source, amount);

        // Descrição formatada do saque
        String withdrawDescription = "Saque no valor de: R$" + (amount/100) + "," + String.format("%02d", amount%100);

        // Realiza o saque e já registra no histórico
        long amountWithdrawn = source.reduceMoney(amount, withdrawDescription);

        return amountWithdrawn;
    }

    /**
     * Realiza uma transferência PIX entre contas.
     *
     * @param sourcePix Chave PIX da conta de origem
     * @param targetPix Chave PIX da conta de destino
     * @param amount Valor da transferência (em centavos)
     * @param transfDescription Descrição da transferência
     * @throws AccountNotFoundException Se alguma conta não for encontrada
     * @throws -NoFundsEnoughException- Se o saldo for insuficiente
     */
    public void transferMoney(final String sourcePix, final String targetPix, final long amount, final String transfDescription) {
        var source = findByPix(sourcePix);
        checkFundsForTransaction(source, amount);
        var target = findByPix(targetPix);

        // Cria mensagens descritivas para ambas as contas com o valor formatado
        String sourceDescription = "Transferência PIX enviada de " + transfDescription + " para conta " + targetPix;
        String targetDescription = "Transferência PIX recebida de " + transfDescription + " da conta " + sourcePix;

        // Realiza a transferência com os registros de histórico
        long transferredAmount = source.reduceMoney(amount, sourceDescription);
        target.addMoney(transferredAmount, targetDescription);
        System.out.println("\n---------------Transferencia Realizada com Sucesso---------------\n");
    }

    /**
     * Busca uma conta bancária pela chave PIX.
     *
     * @param pix Chave PIX da conta
     * @return A conta encontrada
     * @throws AccountNotFoundException Se a conta não for encontrada
     */
    public AccountWallet findByPix(final String pix) {
        return accounts.stream()
                .filter(a -> a.getPix().contains(pix))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada para PIX: " + pix));
    }

    /**
     * Retorna uma lista de todas as contas cadastradas.
     *
     * @return Lista de contas bancárias
     */
    public List<AccountWallet> list() {
        return new ArrayList<>(this.accounts);
    }

    /**
     * Obtém o histórico de transações de uma conta agrupado por data/hora.
     *
     * @param pix Chave PIX da conta
     * @return Mapa de transações agrupadas por timestamp (truncado para segundos)
     * @throws AccountNotFoundException Se a conta não for encontrada
     */
    public Map<OffsetDateTime, List<MoneyAudit>> getHistory(final String pix) {
        var wallet = findByPix(pix);
        return wallet.getFinancialTransactions().stream()
                .collect(Collectors.groupingBy(t -> t.createdAt().truncatedTo(SECONDS)));
    }
}