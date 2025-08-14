package model;

import lombok.Getter;

import java.util.List;

import static model.BankService.ACCOUNT;

@Getter
public class AccountWallet extends Wallet {
    private final List<String> pix;

    public AccountWallet(final long amount, final List<String> pix, final String depositDescription) {
        super(ACCOUNT);
        this.pix = pix;
        this.addMoney(amount, depositDescription);
    }

    @Override
    public String toString() {
        return "AccountWallet{" +
                "pix=" + pix +
                ", balance=R$" + (getFunds() / 100) + "," + String.format("%02d", getFunds() % 100) +
                '}';
    }
}