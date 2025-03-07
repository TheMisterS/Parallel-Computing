import Scenario.Customer;
import Scenario.Shop;
/*
Lab work for parallel computing course
It simulates a shop with a stock array of various products and customers that are able to buy from it if the stock is available
Author: Simonas Jaunius Urbutis
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            System.out.println("Please provide either s or ns as a command line argument");
        }else if(args[0].equals("s") || args[0].equals("ns")) {
            Shop shop = new Shop(10);
            // without sleep possible race condition happens at 100000, not always
            int numberOfCustomers = 30000;
            int transactionsPerCustomer = 100;
            int amountToRestock = 1000;
            Thread[] threads = new Thread[numberOfCustomers];
            for (int i = 0; i < 10; i++) {
                shop.restock(i, amountToRestock);
            }
            if(args[0].equals("s")){
                for (int i = 0; i < numberOfCustomers; i++) {
                    threads[i] = new Thread(new Customer(shop, transactionsPerCustomer, true));
                    threads[i].start();

                }
            }else if(args[0].equals("ns")){
                for (int i = 0; i < numberOfCustomers; i++) {
                    threads[i] = new Thread(new Customer(shop, transactionsPerCustomer, false));
                    threads[i].start();
                }
            }

            for (int i = 0; i < numberOfCustomers; i++) {
                threads[i].join();
            }
            int processors = Runtime.getRuntime().availableProcessors();
            System.out.println("threads at a time: " + processors);
            System.out.println("----------------------------------------------------------");
            System.out.println("Initial restocked value of each item was: " + amountToRestock);
            System.out.println("----------------------------------------------------------");
            shop.printStock();
            System.out.println("----------------------------------------------------------");

        } else {
            System.out.println("Please provide a valid command line argument!");
        }
    }
}
