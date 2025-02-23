import Scenario.BankAccount;
import Scenario.BankTransaction;
/*
Lab work for parallel computing course
It simulates the race condition when two threads access shared resources(critical section) without any solutions,
additionally it shows thread synchronization to solve the issue.
Author: Simonas Jaunius Urbutis
 */
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide either s or ns as a command line argument");
        } else {
            // Synchronized scenario, race condition not raised
            if(args[0].equals("s")){
                System.out.println("Synchronized thread scenario:");
                BankAccount accountTwo = new BankAccount(100);
                Thread t1 = new Thread(new BankTransaction(accountTwo, true), "ThreadThree");
                Thread t2 = new Thread(new BankTransaction(accountTwo, true), "ThreadFour");
                System.out.println("Starting balance(AccountOne): " + accountTwo.getBalance());
                t1.start();
                t2.start();
            }
            // Not-Synchronized scenario, race condition raised
            else if(args[0].equals("ns")){
                System.out.println("Not-Synchronized thread scenario:");
                BankAccount accountOne = new BankAccount(100);
                Thread t1 = new Thread(new BankTransaction(accountOne, false), "ThreadOne");
                Thread t2 = new Thread(new BankTransaction(accountOne, false), "ThreadTwo");
                System.out.println("Starting balance(AccountOne): " + accountOne.getBalance());
                t1.start();
                t2.start();
            } else{
                System.out.println("Please provide a valid command line argument!!");
            }
        }
    }
}