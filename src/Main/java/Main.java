import expcetion.AccountNotFoundException;
import expcetion.NoFundsEnoughException;
import expcetion.WalletNotFoundException;
import model.AccountWallet;
import repository.AccountRepository;
import repository.InvestmentRepository;

import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class Main {

    private final static AccountRepository accountRepository = new AccountRepository();
    private final static InvestmentRepository investmentRepository = new InvestmentRepository();

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("\nBEM VINDO AO PIX BANK\n");
        while (true){
            System.out.println("Selecione a operação desejada");
            System.out.println("1 - Criar uma conta");
            System.out.println("2 - Depositar na conta");
            System.out.println("3 - Sacar da conta");
            System.out.println("4 - Transferencia entre contas");
            System.out.println("5 - Listar contas");
            System.out.println("6 - Historico de conta");
            System.out.println("7 - Criar uma carteira de investimento");
            System.out.println("8 - Criar um Investimento");
            System.out.println("9 - Investir");
            System.out.println("10 - Sacar investimento");
            System.out.println("11 - Atualizar investimentos");
            System.out.println("12 - Listar Investimentos");
            System.out.println("13 - Listar carteiras de investimento");


            System.out.println("14 - Sair");
            var option= scanner.nextInt();
            switch (option){
                case 1 -> createAccount();
                case 2 -> deposit();
                case 3 -> withdraw();
                case 4 -> transferToAccount();
                case 5 -> accountRepository.list().forEach(System.out::println);
                case 6 -> checkHistory();
                case 7 -> createWalletInvestment();
                case 8 -> createInvestment();
                case 9 -> incInvestment();
                case 10 -> rescueInvestment();
                case 11 ->{
                    investmentRepository.updateAmount();
                    System.out.println("Investimentos reajustados");
                }
                case 12 -> investmentRepository.list().forEach(System.out::println);
                case 13 -> investmentRepository.listWallets().forEach(System.out::println);
                case 14 -> System.exit(0);
                default -> System.out.println("Opção inválida");

            }
        }
    }

    private static void createAccount(){
    try{
        System.out.println("Informe as chaves pix (separadas por ';' Exemplo: CPF00000000000;Email@meu.com;tel999999999");
        var pix = Arrays.stream(scanner.next().split(";")).toList();

        // Verifica se há chaves duplicadas
        Set<String> uniqueKeys = new HashSet<>();
        for (String key : pix) {
            if (!uniqueKeys.add(key)) {
                throw new IllegalArgumentException("Chave Pix duplicada encontrada: " + key);
            }
        }

        System.out.println("Informe o valor inicial de deposito \nExemplo: 5000009 Cinquenta mil e nove centavos");
        var amount = scanner.nextLong();
        var wallet = accountRepository.create(pix, amount);
        System.out.println("Conta criada: " + wallet);
    } catch (IllegalArgumentException e) {
        System.err.println("Erro ao criar conta: " + e.getMessage());
        // Aqui você pode adicionar lógica para tentar novamente ou encerrar a operação
    } catch (InputMismatchException e) {
        System.err.println("Valor de depósito inválido. Informe um número válido.");
        scanner.nextLine(); // Limpa o buffer do scanner
    } catch (Exception e) {
        System.err.println("Erro inesperado ao criar conta: " + e.getMessage());
    }
    }

    private static void createInvestment(){
        System.out.println("Informe a taxa do investimento");
        var tax = scanner.nextInt();
        System.out.println("Informe o valor inicial de deposito");
        var initialFunds = scanner.nextLong();
        var investment = investmentRepository.create(tax, initialFunds);
        System.out.println("investimento criado: " + investment);
    }

    private static void withdraw(){
        System.out.println("Informe a chave pix da conta para saque:");
        var pix = scanner.next();
        System.out.println("Informe o valor que será sacado: ");
        var amount = scanner.nextLong();
        try {
            accountRepository.withdraw(pix, amount);
        } catch (NoFundsEnoughException | AccountNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void deposit(){
        System.out.println("Informe a chave pix da conta para deposito:");
        var pix = scanner.next();
        System.out.println("Informe o valor que será depositado: ");
        var amount = scanner.nextLong();
        try{
            accountRepository.deposit(pix, amount);
        } catch (AccountNotFoundException ex){
            System.out.println(ex.getMessage());
        }
    }

    private static void transferToAccount(){
        System.out.println("Informe a chave pix da conta de origgem:");
        var source = scanner.next();
        System.out.println("Informe a chave pix da conta de destino:");
        var target = scanner.next();
        System.out.println("Informe o valor que será depositado: ");
        var amount = scanner.nextLong();
        try{
            accountRepository.transferMoney(source, target, amount);
        } catch (AccountNotFoundException ex){
            System.out.println(ex.getMessage());
        }
    }

    private static void createWalletInvestment(){
        System.out.println("Informe a chave pix da conta:");
        var pix = scanner.next();
        var account = accountRepository.findByPix(pix);
        System.out.println("Informe o identificador do investimento");
        var investmentId = scanner.nextInt();
        var investmentWallet = investmentRepository.initInvestment(account, investmentId);
        System.out.println("Conta de investimento criada: " + investmentWallet);
    }

    private static void incInvestment(){
        System.out.println("Informe a chave pix da conta para investimento:");
        var pix = scanner.next();
        System.out.println("Informe o valor que será investido: ");
        var amount = scanner.nextLong();
        try{
            investmentRepository.deposit(pix, amount);
        } catch (WalletNotFoundException | AccountNotFoundException ex){
            System.out.println(ex.getMessage());
        }
    }

    private static void rescueInvestment(){
        System.out.println("Informe a chave pix da conta para resgate do investimento:");
        var pix = scanner.next();
        System.out.println("Informe o valor que será sacado: ");
        var amount = scanner.nextLong();
        try {
            investmentRepository.withdraw(pix, amount);
        } catch (NoFundsEnoughException | AccountNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void checkHistory(){
        System.out.println("Informe a chave pix da conta para verificar extrato:");
        var pix = scanner.next();
        AccountWallet wallet;
        try {
            var sortedHistory = accountRepository.getHistory(pix);
            sortedHistory.forEach((k, v) -> {
                System.out.println(k.format(ISO_DATE_TIME));
                System.out.println(v.getFirst().transactionId());
                System.out.println(v.getFirst().description());
                System.out.println("R$" + (v.size() / 100) + "," + (v.size() % 100));
            });
        } catch (AccountNotFoundException ex){
            System.out.println(ex.getMessage());
        }
    }

}