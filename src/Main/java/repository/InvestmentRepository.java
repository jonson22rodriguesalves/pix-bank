package repository;

import exception.AccountWithInvestmentException;
import exception.InvestmentNotFoundException;
import exception.WalletNotFoundException;
import model.AccountWallet;
import model.Investment;
import model.InvestmentWallet;

import java.util.ArrayList;
import java.util.List;

import static repository.CommonsRepository.checkFundsForTransaction;

public class InvestmentRepository {

    private long nextId = 0;
    private final List<Investment> investments = new ArrayList<>();
    private final List<InvestmentWallet> wallets =  new ArrayList<>();

    public Investment create(final long tax, final long initialFunds, final String nome){
        this.nextId ++;
        var investment = new Investment(this.nextId, tax, initialFunds, nome);
        investments.add(investment);
        return investment;
    }

    public InvestmentWallet initInvestment(final AccountWallet account, final long id){
        if (!wallets.isEmpty()) {
            var accountsInUse = wallets.stream().map(InvestmentWallet::getAccount).toList();
            if (accountsInUse.contains(account)) {
                throw new AccountWithInvestmentException("A conta'" + account + "'ja possui um investimento");
            }
        }
        var investment = findById(id);
        checkFundsForTransaction(account, investment.initialFunds());
        var wallet = new InvestmentWallet(investment, account, investment.initialFunds());
        wallets.add(wallet);
        return wallet;
    }

    public InvestmentWallet deposit(final String pix, final long funds, final String investmentDescription) {
        var wallet = findWalletByAccountPix(pix);

        // Remove o valor da conta com registro no histórico
        long transferredAmount = wallet.getAccount().reduceMoney(funds, investmentDescription);

        // Descrição para a operação no investimento
        String depositDescription = "Aporte de R$" + (funds / 100) + "," + String.format("%02d", funds % 100);

        // Adiciona na carteira de investimento
        wallet.addMoney(transferredAmount, depositDescription);

        return wallet;
    }

    public InvestmentWallet withdraw(final String pix, final long funds, String investmentDescription) {
        var wallet = findWalletByAccountPix(pix);
        checkFundsForTransaction(wallet, funds);

        // Remove o valor da carteira de investimento e devolve para a conta
        long withdrawnAmount = wallet.reduceMoney(funds);
        // Usa a descrição formatada para o depósito na conta
        wallet.getAccount().addMoney(withdrawnAmount, investmentDescription);

        if (wallet.getFunds() == 0) {
            wallets.remove(wallet);
        }
        return wallet;
    }

    public void updateAmount(){
        wallets.forEach(w -> w.updateAmount(w.getInvestment().tax()));
    }

    public Investment findById(final long id){
        return investments.stream().filter(a -> a.id() == id)
                .findFirst()
                .orElseThrow(
                        () -> new InvestmentNotFoundException("O investimento '" + id + "' nao foi encontrado")
                );
    }

    public InvestmentWallet findWalletByAccountPix(final String pix){
        return wallets.stream()
                .filter(w -> w.getAccount().getPix().contains(pix))
                .findFirst()
                .orElseThrow(
                        () -> new WalletNotFoundException("A carteira nao foi encontrada")
                );
    }

    public List<InvestmentWallet> listWallets(){
        return this.wallets;
    }

    public List<Investment> list(){
        return this.investments;
    }

}