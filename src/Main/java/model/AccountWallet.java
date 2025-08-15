package model;

import lombok.Getter;

import java.util.List;

import static model.BankService.ACCOUNT;

/**
 * Representa uma conta bancária com funcionalidades específicas, incluindo chaves PIX.
 * Herda comportamentos básicos de carteira e adiciona características de conta corrente.
 */
@Getter
public class AccountWallet extends Wallet {
    /**
     * Lista de chaves PIX associadas à conta bancária.
     * Permite identificação da conta para transferências e operações.
     */
    private final List<String> pix;

    /**
     * Constrói uma nova conta bancária com saldo inicial e chaves PIX.
     *
     * @param amount Valor inicial do depósito (em centavos)
     * @param pix Lista de chaves PIX associadas à conta
     * @param depositDescription Descrição do depósito inicial
     */
    public AccountWallet(final long amount, final List<String> pix, final String depositDescription) {
        super(ACCOUNT);
        this.pix = pix;
        this.addMoney(amount, depositDescription);
    }

    /**
     * Retorna uma representação em string da conta bancária.
     * Inclui as chaves PIX e o saldo formatado como valor monetário.
     *
     * @return String formatada com informações da conta
     */
    @Override
    public String toString() {
        return "AccountWallet{" +
                "pix=" + pix +
                ", balance=R$" + (getFunds() / 100) + "," + String.format("%02d", getFunds() % 100) +
                '}';
    }
}