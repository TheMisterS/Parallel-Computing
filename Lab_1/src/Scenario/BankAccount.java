package Scenario;

public class BankAccount {

    // Critical section(shared resource)
    private int balance;

    public BankAccount(int balance) {
        this.balance = balance;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
    // The function updates the account's balance by adding the specified sum and returns the new balance
    public void deposit(int amountToDeposit) {
        int newBalance = balance + amountToDeposit;
        System.out.println(Thread.currentThread().getName() + ": depositing " + amountToDeposit);
        balance = newBalance;
        System.out.println(Thread.currentThread().getName() + ": balance after deposit " + balance);
    }
    // The function updates the account's balance by removing the specified sum and returns the new balance
    public void withdraw(int amountToWithdraw){
        int newBalance = balance - amountToWithdraw;
        System.out.println(Thread.currentThread().getName() + ": withdrawing " + amountToWithdraw);
        balance = newBalance;
        System.out.println(Thread.currentThread().getName() + ": balance after withdrawing " + balance);
    }

    public synchronized void depositSynchronized(int amountToDeposit) {
        int newBalance = balance + amountToDeposit;
        System.out.println(Thread.currentThread().getName() + ": depositing " + amountToDeposit);
        balance = newBalance;
        System.out.println(Thread.currentThread().getName() + ": balance after deposit " + balance);
    }
    // The function updates the account's balance by removing the specified sum and returns the new balance
    public synchronized void withdrawSynchronized(int amountToWithdraw){
        int newBalance = balance - amountToWithdraw;
        System.out.println(Thread.currentThread().getName() + ": withdrawing " + amountToWithdraw);
        balance = newBalance;
        System.out.println(Thread.currentThread().getName() + ": balance after withdrawing " + balance);
    }

}


