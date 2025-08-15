import exception.*;
import model.BankService;
import model.InvestmentWallet;
import model.MoneyAudit;
import repository.AccountRepository;
import repository.InvestmentRepository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

/**
 * Classe principal que representa o sistema bancário PIX Bank.
 * Contém o menu principal e métodos para operações bancárias e de investimento.
 */
public class Main {

    /**
     * Repositório de contas bancárias.
     */
    private final static AccountRepository accountRepository = new AccountRepository();

    /**
     * Repositório de investimentos.
     */
    private final static InvestmentRepository investmentRepository = new InvestmentRepository();

    /**
     * Scanner para entrada de dados do usuário.
     */
    static Scanner scanner = new Scanner(System.in);

    /**
     * Método principal que inicia a aplicação.
     *
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        System.out.println("\n============ MENU PIX BANK ============\n");
        while (true){
            System.out.println("Selecione a opcao desejada");
            System.out.println("1 - Cadastrar nova conta");
            System.out.println("2 - Realizar deposito");
            System.out.println("3 - Realizar saque");
            System.out.println("4 - Transferencia PIX");
            System.out.println("5 - Consultar contas cadastradas");
            System.out.println("6 - Extrato da conta");
            System.out.println("7 - Criar carteira de investimento");
            System.out.println("8 - Cadastrar tipo de investimento");
            System.out.println("9 - Aplicar em investimento");
            System.out.println("10 - Resgatar investimento");
            System.out.println("11 - Atualizar rendimentos");
            System.out.println("12 - Consultar carteira de investimento");
            System.out.println("13 - Listar tipos de investimento");
            System.out.println("14 - Listar todas as carteiras de investimento");
            System.out.println("15 - Sair do sistema");

            var option= scanner.nextInt();
            switch (option){
                case 1 -> createAccount();
                case 2 -> deposit();
                case 3 -> withdraw();
                case 4 -> transferToAccount();
                case 5 -> accountList();
                case 6 -> checkHistory();
                case 7 -> createWalletInvestment();
                case 8 -> createInvestment();
                case 9 -> applyInvestment();
                case 10 -> rescueInvestment();
                case 11 -> updateYield();
                case 12 -> consultWalletInvestment();
                case 13 -> listTypeInvestment();
                case 14 -> listWalletInvestment();
                case 15 -> System.exit(0);
                default -> System.out.println("Opção inválida");

            }
        }
    }

    /**
     * Cria uma nova conta bancária com chaves PIX.
     */
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

    /**
     * Realiza um depósito em uma conta existente.
     */
    private static void deposit() {
        try {
            System.out.println("Informe a chave pix da conta para depósito:");
            // Limpar o buffer do scanner antes de ler
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir a quebra de linha pendente
            }
            String pix = scanner.nextLine().trim();

            if (pix.isEmpty()) {
                System.out.println("Erro: A chave PIX não pode estar vazia.");
                return;
            }

            System.out.println("Informe o valor que será depositado:");
            if (!scanner.hasNextLong()) {
                System.out.println("Erro: Digite um valor válido.");
                scanner.nextLine(); // Limpar o buffer inválido
                return;
            }

            long amount = scanner.nextLong();
            scanner.nextLine(); // Limpa o buffer

            if (amount <= 0) {
                System.out.println("Erro: O valor deve ser positivo maior que zero.");
                return;
            }

            // Formatando a descrição do depósito
            String depositDescription = "Depósito de: R$" + (amount / 100) + "," + String.format("%02d", amount % 100);
            accountRepository.deposit(pix, amount, depositDescription);
            System.out.println("\n--------------- Depósito realizado com sucesso ---------------\n");

        } catch (AccountNotFoundException ex) {
            System.err.println("Erro: Conta não encontrada. " + ex.getMessage());
        } catch (PixInUseException ex) {
            System.err.println("Erro: Chave PIX inválida. " + ex.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erro: Valor numérico muito grande ou inválido");
            System.err.println("Por favor, informe um valor válido (ex: 10000)");
        } catch (Exception ex) {
            System.err.println("Erro inesperado durante o depósito: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Realiza um saque de uma conta existente.
     */
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

    /**
     * Realiza uma transferência PIX entre contas.
     */
    private static void transferToAccount() {
        try {
            System.out.println("Informe a chave pix da conta de origem:");
            // Limpar o buffer do scanner antes de ler
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir a quebra de linha pendente
            }
            String source = scanner.nextLine().trim();

            if (source.isEmpty()) {
                System.out.println("Erro: A chave PIX de origem não pode estar vazia.");
                return;
            }

            System.out.println("Informe a chave pix da conta de destino:");
            String target = scanner.nextLine().trim();

            if (target.isEmpty()) {
                System.out.println("Erro: A chave PIX de destino não pode estar vazia.");
                return;
            }

            System.out.println("Informe o valor que será transferido:");
            if (!scanner.hasNextLong()) {
                System.out.println("Erro: Digite um valor válido.");
                scanner.nextLine(); // Limpar o buffer inválido
                return;
            }

            long amount = scanner.nextLong();
            scanner.nextLine(); // Limpa o buffer

            if (amount <= 0) {
                System.out.println("Erro: O valor deve ser positivo maior que zero.");
                return;
            }

            // Formatando a descrição da transferência
            String transfDescription = " R$" + (amount / 100) + "," + String.format("%02d", amount % 100);
            accountRepository.transferMoney(source, target, amount, transfDescription);


        } catch (AccountNotFoundException ex) {
            System.err.println("Erro: " + ex.getMessage());
        } catch (NoFundsEnoughException ex) {
            System.err.println("Erro: Saldo insuficiente para transferência. " + ex.getMessage());
        } catch (PixInUseException ex) {
            System.err.println("Erro: Chave PIX inválida. " + ex.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erro: Valor numérico muito grande ou inválido");
            System.err.println("Por favor, informe um valor válido (ex: 10000)");
        } catch (Exception ex) {
            System.err.println("Erro inesperado durante a transferência: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Lista todas as contas cadastradas no sistema.
     */
    private static void accountList() {
        try {
            System.out.println("\n--------------- Contas ---------------");

            // Verifica se há contas para listar
            var accounts = accountRepository.list();

            if (accounts.isEmpty()) {
                System.out.println("Nenhuma conta cadastrada.");
            } else {
                accounts.forEach(account -> {
                    String formattedBalance = "R$" + (account.getBalance() / 100) +
                            "," + String.format("%02d", account.getBalance() % 100);
                    System.out.printf("AccountWallet{pix=%s, balance=%s}%n",
                            account.getPix(), formattedBalance);
                });
            }

            System.out.println("--------------------------------------\n");

        } catch (Exception ex) {
            System.err.println("Erro ao listar contas: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Cria uma nova carteira de investimento vinculada a uma conta.
     */
    private static void createWalletInvestment() {
        try {
            System.out.println("Informe a chave pix da conta:");
            // Padrão: limpeza consistente do buffer
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            String pix = scanner.nextLine().trim();

            // Padrão: validação direta com return (igual ao withdraw)
            if (pix.isEmpty()) {
                System.out.println("Erro: A chave PIX não pode estar vazia.");
                return;
            }

            System.out.println("Informe o identificador do investimento:");
            // Padrão: verificação do tipo antes de ler (como no withdraw)
            if (!scanner.hasNextInt()) {
                System.out.println("Erro: Digite um ID válido (número inteiro).");
                scanner.nextLine();
                return;
            }

            int investmentId = scanner.nextInt();
            scanner.nextLine(); // Padrão: limpeza pós leitura numérica

            // Padrão: validação de valor positivo
            if (investmentId <= 0) {
                System.out.println("Erro: O ID deve ser maior que zero.");
                return;
            }

            try {
                // Padrão: operação principal em bloco try separado (como no withdraw)
                var account = accountRepository.findByPix(pix);
                var investment = investmentRepository.findById(investmentId);

                var investmentWallet = investmentRepository.initInvestment(account, investmentId);

                // Formatação mantida mas seguindo o padrão de mensagens
                String formattedValue = "R$" + (investment.initialFunds() / 100) + "," +
                        String.format("%02d", investment.initialFunds() % 100);

                System.out.println("\n--------------- Operação realizada com sucesso ---------------");
                System.out.println("Tipo: Criação de Carteira de Investimento");
                System.out.println("Conta: " + account.getPix());
                System.out.println("Investimento: " + investment.nome() + " (ID: " + investmentId + ")");
                System.out.println("Valor Inicial: " + formattedValue);
                System.out.println("------------------------------------------------------------\n");

                // Histórico (mantido da versão original)
                String description = "Carteira investimento ID " + investmentId +
                        " - Valor inicial: " + formattedValue;
                account.getFinancialTransactions().add(new MoneyAudit(
                        UUID.randomUUID(),
                        BankService.ACCOUNT,
                        description,
                        OffsetDateTime.now()
                ));

            } catch (AccountNotFoundException e) {
                System.out.println("Erro: " + e.getMessage()); // Padrão: println igual ao withdraw
            } catch (AccountWithInvestmentException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (InvestmentNotFoundException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (NoFundsEnoughException e) {
                System.out.println("Erro: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage()); // Padrão igual ao withdraw
            e.printStackTrace();
        }
    }

    /**
     * Cadastra um novo tipo de investimento no sistema.
     */
    private static void createInvestment() {
        try {
            System.out.println("Informe o nome do investimento:");
            // Limpar o buffer do scanner antes de ler
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir a quebra de linha pendente
            }
            var input = scanner.nextLine().trim(); // Usar nextLine() para capturar toda a entrada

            if (input.isEmpty()) {
                System.out.println("Erro: O nome não pode estar vazio.");
                return;
            }

            var nomes = Arrays.stream(input.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (nomes.isEmpty()) {
                System.out.println("Erro: Nenhum nome válido foi informado.");
                return;
            }

            // Verifica nomes duplicados
            Set<String> uniqueNames = new HashSet<>();
            for (String nome : nomes) {
                if (!uniqueNames.add(nome)) {
                    System.out.println("Erro: Nome duplicado encontrado: " + nome);
                    return;
                }
            }

            System.out.println("Informe a taxa do investimento:");
            if (!scanner.hasNextInt()) {
                System.out.println("Erro: Digite um valor válido para a taxa (número inteiro).");
                scanner.nextLine(); // Limpar o buffer inválido
                return;
            }
            var tax = scanner.nextInt();
            scanner.nextLine(); // Limpar o buffer

            if (tax <= 0) {
                System.out.println("Erro: A taxa deve ser maior que zero.");
                return;
            }

            System.out.println("Informe o valor inicial de depósito:");
            if (!scanner.hasNextLong()) {
                System.out.println("Erro: Digite um valor válido.");
                scanner.nextLine(); // Limpar o buffer inválido
                return;
            }
            var initialFunds = scanner.nextLong();
            scanner.nextLine(); // Limpar o buffer

            if (initialFunds <= 0) {
                System.out.println("Erro: O valor deve ser maior que zero.");
                return;
            }

            try {
                var investment = investmentRepository.create(tax, initialFunds, String.join(";", nomes));
                // Formatando o valor para exibição
                String formattedValue = "R$" + (initialFunds / 100) + "," + String.format("%02d", initialFunds % 100);

                System.out.println("\n--------------- Investimento criado com sucesso ---------------");
                System.out.println("ID: " + investment.id());
                System.out.println("Nome(s): " + String.join(", ", nomes));
                System.out.println("Taxa: " + tax + "%");
                System.out.println("Valor Inicial: " + formattedValue);
                System.out.println("--------------------------------------------------------------\n");

            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Realiza uma aplicação em um investimento existente.
     */
    private static void applyInvestment() {
        try {
            System.out.println("Informe a chave pix da conta para investimento:");
            // Padrão: limpeza consistente do buffer
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            var pix = scanner.nextLine().trim();

            // Padrão: validação direta com return
            if (pix.isEmpty()) {
                System.out.println("Erro: A chave PIX não pode estar vazia.");
                return;
            }

            System.out.println("Informe o valor que será investido:");
            // Padrão: verificação do tipo antes de ler
            if (!scanner.hasNextLong()) {
                System.out.println("Erro: Digite um valor válido.");
                scanner.nextLine();
                return;
            }

            var amount = scanner.nextLong();
            scanner.nextLine(); // Padrão: limpeza pós leitura numérica

            // Padrão: validação de valor positivo
            if (amount <= 0) {
                System.out.println("Erro: O valor deve ser maior que zero.");
                return;
            }

            try {
                // Padrão: operação principal em bloco try separado
                String description = "Aporte em Investimento de: R$" +
                        (amount / 100) + "," +
                        String.format("%02d", amount % 100);

                investmentRepository.deposit(pix, amount, description);

                System.out.println("\n--------------- Investimento realizado com sucesso ---------------");
                System.out.println("Conta PIX: " + pix);
                System.out.println("Valor: R$" + (amount / 100) + "," + String.format("%02d", amount % 100));
                System.out.println("------------------------------------------------------------------\n");

            } catch (WalletNotFoundException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (AccountNotFoundException e) {
                System.out.println("Erro: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Realiza o resgate de um investimento existente.
     */
    private static void rescueInvestment() {
        try {
            System.out.println("Informe a chave pix da conta para resgate:");
            // Padronização: limpeza do buffer antes da leitura
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir quebra de linha pendente
            }
            var pix = scanner.nextLine().trim(); // Padrão nextLine() + trim()

            // Validação padrão como no withdraw
            if (pix.isEmpty()) {
                System.out.println("Erro: A chave PIX não pode estar vazia.");
                return;
            }

            System.out.println("Informe o valor que será resgatado:");
            // Validação de tipo idêntica ao withdraw
            if (!scanner.hasNextLong()) {
                System.out.println("Erro: Digite um valor válido.");
                scanner.nextLine(); // Limpar buffer inválido
                return;
            }

            var amount = scanner.nextLong();
            scanner.nextLine(); // Limpeza padrão do buffer

            // Validação de valor positivo idêntica
            if (amount <= 0) {
                System.out.println("Erro: O valor deve ser maior que zero.");
                return;
            }

            try {
                // Formatação mantida mas seguindo o padrão
                String description = "Resgate de investimento: R$" +
                        (amount / 100) + "," +
                        String.format("%02d", amount % 100);

                investmentRepository.withdraw(pix, amount, description);

                // Mensagem de sucesso padronizada
                System.out.println("\n--------------- Resgate realizado com sucesso ---------------");
                System.out.println("Conta: " + pix);
                System.out.println("Valor resgatado: R$" + (amount / 100) + "," +
                        String.format("%02d", amount % 100));
                System.out.println("------------------------------------------------------------\n");

            } catch (NoFundsEnoughException | AccountNotFoundException ex) {
                // Tratamento idêntico ao withdraw
                System.out.println(ex.getMessage());
            }

        } catch (Exception e) {
            // Bloco catch padrão igual ao withdraw
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Atualiza os rendimentos de todas as carteiras de investimento.
     */
    private static void updateYield() {
        try {
            System.out.println("Confirma a atualização dos rendimentos? (S/N)");
            // Padronização: limpeza do buffer antes da leitura
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir quebra de linha pendente
            }
            var confirmation = scanner.nextLine().trim().toUpperCase();

            // Validação padrão como no withdraw
            if (confirmation.isEmpty()) {
                System.out.println("Erro: Confirmação não pode estar vazia.");
                return;
            }

            if (!confirmation.equals("S")) {
                System.out.println("Operação cancelada pelo usuário.");
                return;
            }

            try {
                // Operação principal
                investmentRepository.updateAmount();

                // Mensagem de sucesso padronizada
                System.out.println("\n--------------- Rendimentos atualizados com sucesso ---------------");
                System.out.println("Data: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                System.out.println("----------------------------------------------------------------\n");

            } catch (Exception ex) {
                // Tratamento padrão para erros na operação
                System.out.println("Erro durante a atualização: " + ex.getMessage());
            }

        } catch (Exception e) {
            // Bloco catch padrão para erros inesperados
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Consulta carteiras de investimento, com opção de filtro por chave PIX.
     */
    private static void consultWalletInvestment() {
        try {
            System.out.println("Deseja filtrar por chave PIX? (S/N)");
            // Padronização: limpeza do buffer antes da leitura
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir quebra de linha pendente
            }
            var filterOption = scanner.nextLine().trim().toUpperCase();

            try {
                List<InvestmentWallet> wallets = investmentRepository.listWallets();

                System.out.println("\n--------------- Carteiras de Investimento ---------------");

                if (wallets.isEmpty()) {
                    System.out.println("Nenhuma carteira de investimento encontrada.");
                } else {
                    // Filtra por PIX se o usuário escolheu 'S'
                    if (filterOption.equals("S")) {
                        System.out.println("Informe a chave PIX para filtro:");
                        var pixFilter = scanner.nextLine().trim();

                        wallets = wallets.stream()
                                .filter(w -> w.getAccount().getPix().contains(pixFilter))
                                .toList();
                    }

                    wallets.forEach(wallet -> {
                        System.out.println("Conta PIX: " + wallet.getAccount().getPix().get(0));
                        System.out.println("Investimento: " + wallet.getInvestment().nome());
                        System.out.println("Taxa: " + wallet.getInvestment().tax() + "%");
                        System.out.println("Saldo Investido: R$" +
                                (wallet.getFunds() / 100) + "," +
                                String.format("%02d", wallet.getFunds() % 100));
                        System.out.println("Saldo Disponível: R$" +
                                (wallet.getAccount().getFunds() / 100) + "," +
                                String.format("%02d", wallet.getAccount().getFunds() % 100));
                        System.out.println("--------------------------------------------------");
                    });
                }

                System.out.println("--------------------------------------------------------\n");

            } catch (Exception e) {
                System.out.println("Erro ao consultar investimentos: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Lista todos os tipos de investimento disponíveis no sistema.
     */
    private static void listTypeInvestment() {
        try {
            System.out.println("Deseja listar todos os tipos de investimento? (S/N)");
            // Padronização: limpeza do buffer antes da leitura
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir quebra de linha pendente
            }
            var confirmation = scanner.nextLine().trim().toUpperCase();

            // Validação padrão como no withdraw
            if (confirmation.isEmpty()) {
                System.out.println("Erro: Confirmação não pode estar vazia.");
                return;
            }

            if (!confirmation.equals("S")) {
                System.out.println("Operação cancelada pelo usuário.");
                return;
            }

            try {
                var investments = investmentRepository.list();

                System.out.println("\n--------------- Tipos de Investimento Disponíveis ---------------");

                if (investments.isEmpty()) {
                    System.out.println("Nenhum tipo de investimento cadastrado.");
                } else {
                    investments.forEach(investment -> {
                        System.out.println("ID: " + investment.id());
                        System.out.println("Nome: " + investment.nome());
                        System.out.println("Taxa: " + investment.tax() + "%");
                        System.out.println("Valor Mínimo: R$" + (investment.initialFunds() / 100) + "," +
                                String.format("%02d", investment.initialFunds() % 100));
                        System.out.println("--------------------------------------------------------");
                    });
                }

                System.out.println("----------------------------------------------------------------\n");

            } catch (Exception ex) {
                System.out.println("Erro ao listar investimentos: " + ex.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Lista todas as carteiras de investimento do sistema.
     */
    private static void listWalletInvestment() {
        try {
            System.out.println("Deseja listar todas as carteiras de investimento? (S/N)");
            // Padronização: limpeza do buffer antes da leitura
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Consumir quebra de linha pendente
            }
            var confirmation = scanner.nextLine().trim().toUpperCase();

            // Validação padrão como no withdraw
            if (confirmation.isEmpty()) {
                System.out.println("Erro: Confirmação não pode estar vazia.");
                return;
            }

            if (!confirmation.equals("S")) {
                System.out.println("Operação cancelada pelo usuário.");
                return;
            }

            try {
                var wallets = investmentRepository.listWallets();

                System.out.println("\n--------------- Carteiras de Investimento ---------------");

                if (wallets.isEmpty()) {
                    System.out.println("Nenhuma carteira de investimento encontrada.");
                } else {
                    wallets.forEach(wallet -> {
                        System.out.println("Conta PIX: " + wallet.getAccount().getPix().get(0));
                        System.out.println("Tipo de Investimento: " + wallet.getInvestment().nome());
                        System.out.println("Taxa: " + wallet.getInvestment().tax() + "%");
                        System.out.println("Saldo Investido: R$" + (wallet.getFunds() / 100) + "," +
                                String.format("%02d", wallet.getFunds() % 100));
                        System.out.println("Saldo Disponível: R$" +
                                (wallet.getAccount().getFunds() / 100) + "," +
                                String.format("%02d", wallet.getAccount().getFunds() % 100));
                        System.out.println("--------------------------------------------------");
                    });
                }

                System.out.println("--------------------------------------------------------\n");

            } catch (Exception ex) {
                System.out.println("Erro ao listar carteiras: " + ex.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Exibe o histórico de transações e extrato de uma conta.
     */
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
                    System.out.println("Nenhuma transação registrada.");
                } else {
                    System.out.println("Histórico:");
                    System.out.println("------------------------------------------------");
                    transactions.forEach(t -> System.out.println(
                            t.createdAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " | " +
                                    t.description()
                    ));
                }

                System.out.println("------------------------------------------------\n");

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