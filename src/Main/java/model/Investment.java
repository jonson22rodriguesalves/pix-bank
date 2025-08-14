package model;

public record Investment(
        long id,
        long tax,
        long initialFunds,
        String nome)
{

    @Override
    public String toString() {
        return "Investment{" +
                "id=" + id +
                ", nome=" + nome +
                ", tax=" + tax + "%" +
                ", initialFunds=" + (initialFunds / 100) + "," + (initialFunds % 100) +
                '}';
    }
}