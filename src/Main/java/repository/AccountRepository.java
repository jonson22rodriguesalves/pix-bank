package repository;

import exception.AccountNotFoundException;
import exception.PixInUseException;
import model.AccountWallet;
import model.MoneyAudit;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static repository.CommonsRepository.checkFundsForTransaction;
import static java.time.temporal.ChronoUnit.SECONDS;

public class AccountRepository {
    private final List<AccountWallet> accounts = new ArrayList<>();

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

    public void deposit(final String pix, final long fundsAmount, final String depositDescription) {
        var target = findByPix(pix);
        target.addMoney(fundsAmount, depositDescription);
    }

    public long withdraw(final String pix, final long amount) {
        var source = findByPix(pix);
        checkFundsForTransaction(source, amount);

        // Descrição formatada do saque
        String withdrawDescription = "Saque no valor de: R$" + (amount/100) + "," + String.format("%02d", amount%100);

        // Realiza o saque e já registra no histórico
        long amountWithdrawn = source.reduceMoney(amount, withdrawDescription);

        return amountWithdrawn;
    }

    public void transferMoney(final String sourcePix, final String targetPix, final long amount,final String transfDescription) {
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
    public AccountWallet findByPix(final String pix) {
        return accounts.stream()
                .filter(a -> a.getPix().contains(pix))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Conta não encontrada para PIX: " + pix));
    }

    public List<AccountWallet> list() {
        return new ArrayList<>(this.accounts);
    }

    public Map<OffsetDateTime, List<MoneyAudit>> getHistory(final String pix) {
        var wallet = findByPix(pix);
        return wallet.getFinancialTransactions().stream()
                .collect(Collectors.groupingBy(t -> t.createdAt().truncatedTo(SECONDS)));
    }
}