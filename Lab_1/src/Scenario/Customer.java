package Scenario;

import java.util.Random;

public class Customer implements Runnable {
    private Shop shop;
    private int transactions;
    private Random rand = new Random();
    private boolean synchronizationFlag;

    public Customer(Shop shop, int transactions, boolean synchronizationFlag) {
        this.shop = shop;
        this.transactions = transactions;
        this.synchronizationFlag = synchronizationFlag;
    }

    @Override
    public void run() {
        for (int i = 0; i < transactions; i++) {
            int productIndex = rand.nextInt(10);
            int quantity = rand.nextInt(10) + 1;
                if(synchronizationFlag) {
                    boolean success = shop.buyS(productIndex, quantity);
                }else {
                    boolean success = shop.buyNS(productIndex, quantity);
                }
            }
        }
    }

