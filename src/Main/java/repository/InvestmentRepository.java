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

/**
 * Repositório responsável pela gestão de investimentos e carteiras de investimento.
 * Mantém os registros de tipos de investimento e carteiras associadas a contas.
 */
public class InvestmentRepository {

    /**
     * Contador para geração do próximo ID de investimento.
     */
    private long nextId = 0;

    /**
     * Lista de tipos de investimento cadastrados no sistema.
     */
    private final List<Investment> investments = new ArrayList<>();

    /**
     * Lista de carteiras de investimento associadas a contas.
     */
    private final List<InvestmentWallet> wallets = new ArrayList<>();

    /**
     * Cria um novo tipo de investimento no sistema.
     *
     * @param tax Taxa de rendimento do investimento (em porcentagem)
     * @param initialFunds Valor mínimo inicial para aplicação (em centavos)
     * @param nome Nome(s) do tipo de investimento
     * @return O investimento criado
     */
    public Investment create(final long tax, final long initialFunds, final String nome) {
        this.nextId++;
        var investment = new Investment(this.nextId, tax, initialFunds, nome);
        investments.add(investment);
        return investment;
    }

    /**
     * Inicializa uma nova carteira de investimento para uma conta.
     *
     * @param account Conta associada à carteira de investimento
     * @param id ID do tipo de investimento
     * @return A carteira de investimento criada
     * @throws AccountWithInvestmentException Se a conta já possui uma carteira de investimento
     * @throws InvestmentNotFoundException Se o tipo de investimento não for encontrado
     */
    public InvestmentWallet initInvestment(final AccountWallet account, final long id) {
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

    /**
     * Realiza um depósito na carteira de investimento associada a uma conta.
     *
     * @param pix Chave PIX da conta
     * @param funds Valor a ser depositado (em centavos)
     * @param investmentDescription Descrição da operação
     * @return A carteira de investimento atualizada
     * @throws WalletNotFoundException Se a carteira não for encontrada
     */
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

    /**
     * Realiza um resgate da carteira de investimento para a conta associada.
     *
     * @param pix Chave PIX da conta
     * @param funds Valor a ser resgatado (em centavos)
     * @param investmentDescription Descrição da operação
     * @return A carteira de investimento atualizada
     * @throws WalletNotFoundException Se a carteira não for encontrada
     */
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

    /**
     * Atualiza os valores das carteiras de investimento aplicando a taxa de rendimento.
     */
    public void updateAmount() {
        wallets.forEach(w -> w.updateAmount(w.getInvestment().tax()));
    }

    /**
     * Busca um tipo de investimento pelo ID.
     *
     * @param id ID do investimento
     * @return O investimento encontrado
     * @throws InvestmentNotFoundException Se o investimento não for encontrado
     */
    public Investment findById(final long id) {
        return investments.stream().filter(a -> a.id() == id)
                .findFirst()
                .orElseThrow(
                        () -> new InvestmentNotFoundException("O investimento '" + id + "' nao foi encontrado")
                );
    }

    /**
     * Busca uma carteira de investimento pela chave PIX da conta associada.
     *
     * @param pix Chave PIX da conta
     * @return A carteira de investimento encontrada
     * @throws WalletNotFoundException Se a carteira não for encontrada
     */
    public InvestmentWallet findWalletByAccountPix(final String pix) {
        return wallets.stream()
                .filter(w -> w.getAccount().getPix().contains(pix))
                .findFirst()
                .orElseThrow(
                        () -> new WalletNotFoundException("A carteira nao foi encontrada")
                );
    }

    /**
     * Retorna a lista de todas as carteiras de investimento.
     *
     * @return Lista de carteiras de investimento
     */
    public List<InvestmentWallet> listWallets() {
        return this.wallets;
    }

    /**
     * Retorna a lista de todos os tipos de investimento cadastrados.
     *
     * @return Lista de investimentos
     */
    public List<Investment> list() {
        return this.investments;
    }
}