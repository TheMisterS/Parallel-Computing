package Scenario;

public class BankTransaction implements Runnable {
    private BankAccount account;
    boolean synchronizationFlag;

    public BankTransaction(BankAccount account, boolean synchronizationFlag) {
        this.account = account;
        this.synchronizationFlag = synchronizationFlag;
    }

    @Override
    public void run(){
        if (synchronizationFlag) {
            account.depositSynchronized(50);
            account.withdrawSynchronized(50);
        }else{
            account.deposit(50);
            account.withdraw(50);
        }

    }
}
