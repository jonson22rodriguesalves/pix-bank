import exception.*;
import model.AccountWallet;
import model.BankService;
import model.MoneyAudit;
import repository.AccountRepository;
import repository.InvestmentRepository;

import java.time.OffsetDateTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class Main {

    private final static AccountRepository accountRepository = new AccountRepository();
    private final static InvestmentRepository investmentRepository = new InvestmentRepository();

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("\nBEM VINDO AO PIX BANK\n");
        while (true){
            System.out.println("Selecione a opcao desejada");
            System.out.println("1 - Criar uma conta");
            System.out.println("2 - Depositar na conta");
            System.out.println("3 - Sacar da conta");
            System.out.println("4 - Transferencia entre contas");
            System.out.println("5 - Listar contas");
            System.out.println("6 - Historico de conta");
            System.out.println("7 - Criar uma carteira de investimento");
            System.out.println("8 - Criar uma modalidade de Investimento");
            System.out.println("9 - Investir");
            System.out.println("10 - Sacar investimento");
            System.out.println("11 - Atualizar investimentos");
            System.out.println("12 - Listar modalidade de Investimento");
            System.out.println("13 - Listar carteiras de investimento");


            System.out.println("14 - Sair");
            var option= scanner.nextInt();
            switch (option){
                case 1 -> createAccount();
                case 2 -> deposit();
                case 3 -> withdraw();
                case 4 -> transferToAccount();
                case 5 -> listarConta();
                case 6 -> checkHistory();
                case 7 -> createWalletInvestment();
                case 8 -> createInvestment();
                case 9 -> investment();
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

    private static void listarConta(){
        System.out.println("\n--------------- Contas ---------------");
        accountRepository.list().forEach(account ->
                System.out.println("AccountWallet{pix=" + account.getPix() +
                        ", balance=R$" + (account.getBalance() / 100) +
                        "," + String.format("%02d", account.getBalance() % 100) + "}")
        );
        System.out.println("--------------------------------------\n");
    }

    private static void createAccount() {
        try {
            System.out.println("Informe as chaves pix (separadas por ';' Exemplo: CPF00000000000;Email@meu.com;tel999999999");
            var pix = Arrays.stream(scanner.next().split(";")).toList();

            if (pix.isEmpty()) {
                throw new IllegalArgumentException("Nenhuma chave Pix válida foi informada");
            }

            // Verifica se há chaves duplicadas
            Set<String> uniqueKeys = new HashSet<>();
            for (String key : pix) {
                if (!uniqueKeys.add(key)) {
                    throw new IllegalArgumentException("Chave Pix duplicada encontrada: " + key);
                }
            }

            long amount = 0;
            boolean validAmount = false;

            while (!validAmount) {
                try {
                    System.out.println("Informe o valor inicial de depósito (apenas números)");
                    System.out.println("Exemplo: 5000009 (para cinquenta mil e nove centavos)");

                    String input = scanner.next().trim();

                    // Remove todos os caracteres não numéricos
                    String numericValue = input.replaceAll("[^0-9]", "");
                    scanner.nextLine();
                    if (numericValue.isEmpty()) {
                        System.err.println("Erro: Nenhum valor numérico foi informado");
                        continue;
                    }

                    amount = Long.parseLong(numericValue);
                    validAmount = true;

                } catch (NumberFormatException e) {
                    System.err.println("Erro: Valor numérico muito grande ou inválido");
                    System.err.println("Por favor, informe um valor válido (ex: 10000)");
                }
            }

            // Formatando a descrição do depósito inicial com o valor
            String depositDescription = "Depósito inicial: R$" + (amount / 100) + "," + String.format("%02d", amount % 100);
            var wallet = accountRepository.create(pix, amount, depositDescription);
            System.out.println("\n--------------- Conta criada com sucesso ---------------\n" + wallet + "\n");

        } catch (IllegalArgumentException e) {
            System.err.println("Erro na criação da conta: " + e.getMessage());
            System.err.println("Por favor, tente novamente com dados válidos");
        } catch (InputMismatchException e) {
            System.err.println("Erro: Falha na abertura de conta Valor de deposito invalido.");
            scanner.nextLine(); // Limpa o buffer do scanner
        } catch (Exception e) {
            System.err.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void deposit() {
        try {
            System.out.println("Informe a chave PIX da conta para deposito:");
            String pix = scanner.next();

            System.out.println("Informe o valor que sera depositado:");

            // Verificação mais robusta da entrada
            while (!scanner.hasNextLong()) {
                System.err.println("Erro: Valor invalido. Tente novamente Digite apenas numeros.");
                scanner.next(); // Descarta a entrada inválida
                System.out.println("Informe o valor que sera depositado:");
            }

            long amount = scanner.nextLong();
            scanner.nextLine(); // Limpa o buffer

            if (amount <= 0) {
                throw new IllegalArgumentException("O valor do deposito deve ser positivo.");
            }
            // Formatando a descrição do depósito inicial com o valor
            String depositDescription = "Depósito de: R$" + (amount / 100) + "," + String.format("%02d", amount % 100);
            accountRepository.deposit(pix, amount, depositDescription);
            System.out.println("\n--------------- Deposito realizado com sucesso ---------------\n");
        }
        catch (AccountNotFoundException ex) {
            System.err.println("Erro: Conta nao encontrada. " + ex.getMessage());
        }
        catch (PixInUseException ex) {
            System.err.println("Erro: Chave PIX invalida. " + ex.getMessage());
        }
        catch (IllegalArgumentException ex) {
            System.err.println("Erro: " + ex.getMessage());
        }
        catch (Exception ex) {
            System.err.println("Erro inesperado durante o deposito: " + ex.getMessage());
            ex.printStackTrace();
            // Garante que o scanner esteja em estado válido
            if (scanner != null) {
                scanner.nextLine();
            }
        }
    }

    private static void withdraw(){
        try{
        System.out.println("Informe a chave pix da conta para saque:");
            // Limpar o buffer do scanner antes de ler
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir a quebra de linha pendente
            }
        var pix = scanner.nextLine().trim(); // Usar nextLine() para capturar toda a entrada e trim() para remover espaços
            if (pix.isEmpty()) {
                System.out.println("Erro: A chave PIX nao pode estar vazia.");
                return;
            }
            System.out.println("Informe o valor que sera sacado: ");
            if (!scanner.hasNextLong()) {
                System.out.println("Erro: Digite um valor valido.");
                scanner.nextLine(); // Limpar o buffer inválido
                return;
            }
            var amount = scanner.nextLong();
            scanner.nextLine(); // Limpa o buffer
            if (amount<=0) {
                System.out.println("Erro: O valor deve ser maior que zero.");
                return;
            }

        try {
            accountRepository.withdraw(pix, amount);
            System.out.println("\n--------------- Saque realizado com sucesso ---------------\n");
        } catch (NoFundsEnoughException | AccountNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    private static void transferToAccount(){
        System.out.println("Informe a chave pix da conta de origem:");
        var source = scanner.next();
        System.out.println("Informe a chave pix da conta de destino:");
        var target = scanner.next();
        System.out.println("Informe o valor que sera depositado: ");
        var amount = scanner.nextLong();
        // Formatando a descrição do depósito inicial com o valor
        String transfDescription = " R$" + (amount / 100) + "," + String.format("%02d", amount % 100);
        try{
            accountRepository.transferMoney(source, target, amount, transfDescription);
        } catch (AccountNotFoundException ex){
            System.out.println(ex.getMessage());
        }
    }

    private static void createWalletInvestment() {
        try {
            System.out.println("Informe a chave pix da conta:");
            // Limpar buffer do scanner
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            var pix = scanner.nextLine().trim();

            if (pix.isEmpty()) {
                throw new IllegalArgumentException("Nenhuma chave Pix válida foi informada");
            }

            int investmentId = 0;
            boolean validInput = false;

            while (!validInput) {
                try {
                    System.out.println("Informe o identificador do investimento:");
                    String input = scanner.nextLine().trim();

                    if (input.isEmpty()) {
                        System.err.println("Erro: O ID do investimento não pode estar vazio");
                        continue;
                    }

                    investmentId = Integer.parseInt(input);

                    if (investmentId <= 0) {
                        System.err.println("Erro: O ID do investimento deve ser um número positivo");
                        continue;
                    }

                    validInput = true;
                } catch (NumberFormatException e) {
                    System.err.println("Erro: O ID do investimento deve ser um número válido");
                    System.err.println("Por favor, digite apenas números (ex: 1, 2, 3...)");
                }
            }

            var account = accountRepository.findByPix(pix);
            var investment = investmentRepository.findById(investmentId);

            // Formatando a descrição do investimento inicial
            String investmentDescription = "Aplicação inicial em investimento " + investmentId +
                    " no valor de R$" + (investment.initialFunds() / 100) +
                    "," + String.format("%02d", investment.initialFunds() % 100);
            var investmentWallet = investmentRepository.initInvestment(account, investmentId);

            System.out.println("\n--------------- Carteira de Investimento Criada com Sucesso ---------------");
            System.out.println("Conta: " + account.getPix());
            System.out.println("Investimento ID: " + investmentId);
            System.out.println("Investimento nome: " + investment.nome());
            System.out.println("Saldo inicial: R$" + (investmentWallet.getFunds() / 100) + "," +
                    String.format("%02d", investmentWallet.getFunds() % 100));
            System.out.println("---------------------------------------------------------------------------\n");

            // Adicionando registro formatado no histórico da conta
            String accountDescription = "Criação de carteira de investimento ID " + investmentId +
                    " com valor inicial de R$" + (investment.initialFunds() / 100) +
                    "," + String.format("%02d", investment.initialFunds() % 100);

            account.getFinancialTransactions().add(new MoneyAudit(
                    UUID.randomUUID(),
                    BankService.ACCOUNT,
                    accountDescription,
                    OffsetDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            System.err.println("Erro na criação da carteira de investimento: " + e.getMessage());
            System.err.println("Por favor, tente novamente com dados válidos");
        } catch (AccountNotFoundException e) {
            System.err.println("Erro: Conta não encontrada. " + e.getMessage());
        } catch (AccountWithInvestmentException e) {
            System.err.println("Erro: " + e.getMessage());
            System.err.println("Cada conta pode ter apenas uma carteira de investimento");
        } catch (InvestmentNotFoundException e) {
            System.err.println("Erro: " + e.getMessage());
            System.err.println("Verifique o ID do investimento e tente novamente");
        } catch (Exception e) {
            System.err.println("Erro inesperado ao criar carteira de investimento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createInvestment(){
        try {
            System.out.println("Informe o nome");
            var nome = Arrays.stream(scanner.next().split(";")).toList();

            if (nome.isEmpty()) {
                throw new IllegalArgumentException("Nenhum nome valido foi informado");
            }

            // Verifica se há chaves duplicadas
            Set<String> uniqueKeys = new HashSet<>();
            for (String nomeInvestimento : nome) {
                if (!uniqueKeys.add(nomeInvestimento)) {
                    throw new IllegalArgumentException("Chave Pix duplicada encontrada: " + nomeInvestimento);
                }
            }

        System.out.println("Informe a taxa do investimento");
        var tax = scanner.nextInt();
        System.out.println("Informe o valor inicial de deposito");
        var initialFunds = scanner.nextLong();
        var investment = investmentRepository.create(tax, initialFunds, String.valueOf(nome));
        System.out.println("\n------------------------- investimento criado -------------------------\n" + investment);
        System.out.println("-----------------------------------------------------------------------\n");

        }catch (IllegalArgumentException e) {
            System.err.println("Erro na criação do nome do investimento: " + e.getMessage());
            System.err.println("Por favor, tente novamente com dados válidos");
        }
    }

    private static void investment() {
        try {
            System.out.println("Informe a chave pix da conta para investimento:");
            // Limpar buffer do scanner
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            var pix = scanner.nextLine().trim();

            if (pix.isEmpty()) {
                throw new IllegalArgumentException("A chave PIX não pode estar vazia");
            }

            long amount = 0;
            boolean validAmount = false;

            while (!validAmount) {
                try {
                    System.out.println("Informe o valor que será investido (apenas números):");
                    System.out.println("Exemplo: 5000009 (para cinquenta mil e nove centavos)");

                    String input = scanner.nextLine().trim();

                    // Remove todos os caracteres não numéricos
                    String numericValue = input.replaceAll("[^0-9]", "");

                    if (numericValue.isEmpty()) {
                        System.err.println("Erro: Nenhum valor numérico foi informado");
                        continue;
                    }

                    amount = Long.parseLong(numericValue);

                    if (amount <= 0) {
                        System.err.println("Erro: O valor do investimento deve ser positivo");
                        continue;
                    }

                    validAmount = true;

                } catch (NumberFormatException e) {
                    System.err.println("Erro: Valor numérico muito grande ou inválido");
                    System.err.println("Por favor, informe um valor válido (ex: 10000)");
                }
            }

            // Formatando a descrição do investimento
            String investmentDescription = "Investimento de: R$" + (amount / 100) + "," + String.format("%02d", amount % 100);

            investmentRepository.deposit(pix, amount);

            System.out.println("\n--------------- Investimento Realizado com Sucesso ---------------");
            System.out.println("Conta PIX: " + pix);
            System.out.println("Valor investido: R$" + (amount / 100) + "," + String.format("%02d", amount % 100));
            System.out.println("------------------------------------------------------------------\n");

        } catch (IllegalArgumentException e) {
            System.err.println("Erro no investimento: " + e.getMessage());
            System.err.println("Por favor, tente novamente com dados válidos");
        } catch (WalletNotFoundException e) {
            System.err.println("Erro: Carteira de investimento não encontrada. " + e.getMessage());
            System.err.println("Verifique se a conta possui uma carteira de investimento criada");
        } catch (AccountNotFoundException e) {
            System.err.println("Erro: Conta não encontrada. " + e.getMessage());
            System.err.println("Verifique a chave PIX informada");
        } catch (Exception e) {
            System.err.println("Erro inesperado ao realizar investimento: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void rescueInvestment(){
        System.out.println("Informe a chave pix da conta para resgate do investimento:");
        var pix = scanner.next();
        System.out.println("Informe o valor que sera sacado: ");
        var amount = scanner.nextLong();
        // Formatando a descrição do investimento
        String investmentDescription = "Investimento de: R$" + (amount / 100) + "," + String.format("%02d", amount % 100);
        try {
            investmentRepository.withdraw(pix, amount, investmentDescription);
        } catch (NoFundsEnoughException | AccountNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void checkHistory() {
        try {
            System.out.println("Informe a chave pix da conta para verificar extrato:");
            // Limpar o buffer do scanner antes de ler
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir a quebra de linha pendente
            }

            var pix = scanner.nextLine().trim();

            if (pix.isEmpty()) {
                System.out.println("Erro: A chave PIX não pode estar vazia.");
                return;
            }

            try {
                var account = accountRepository.findByPix(pix);
                var transactions = account.getFinancialTransactions();

                if (transactions.isEmpty()) {
                    System.out.println("Nenhum histórico encontrado para esta conta.");
                    return;
                }

                System.out.println("\n=== EXTRATO BANCÁRIO ===");
                System.out.println("Chave PIX: " + pix);
                System.out.println("Saldo atual: R$" + (account.getFunds() / 100) + "," + String.format("%02d", account.getFunds() % 100));
                System.out.println("-------------------------");
                System.out.println("Histórico de Transações:");
                System.out.println("-------------------------");

                transactions.forEach(transaction -> {
                    System.out.println("Data: " + transaction.createdAt().format(ISO_DATE_TIME));
                    System.out.println("ID: " + transaction.transactionId());
                    System.out.println("Descrição: " + transaction.description());
                    System.out.println("-------------------------");
                });

            } catch (AccountNotFoundException ex) {
                System.out.println(ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Erro ao acessar o histórico da conta: " + ex.getMessage());
            }

        } catch (Exception ex) {
            System.out.println("Erro inesperado: " + ex.getMessage());
        }
    }

}