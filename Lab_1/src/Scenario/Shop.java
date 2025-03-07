package Scenario;

public class Shop {
    private int[] stock;
    private int[] totalSales;


    public Shop(int numberOfProducts) {
        stock = new int[numberOfProducts];
        totalSales = new int[numberOfProducts];
    }
    // Critical section
    public synchronized boolean  buyS(int productIndex, int quantity) {
        if (stock[productIndex] >= quantity) {
            stock[productIndex] -= quantity;
            updateTotalSales(productIndex, quantity);
            return true;
        }
        return false;
    }

    public  boolean  buyNS(int productIndex, int quantity) {
        if (stock[productIndex] >= quantity) {
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            stock[productIndex] -= quantity;
            updateTotalSales(productIndex, quantity);
            return true;
        }
        return false;
    }

    public  void restock(int productIndex, int quantity) {
        stock[productIndex] += quantity;
    }

    public void printStock() {
        System.out.println("Final stock levels:");
        for (int i = 0; i < stock.length; i++) {
            System.out.println("Product stock " + i + ": " + stock[i] + " Product sales: " + totalSales[i]);
        }
    }

    private void updateTotalSales(int productIndex,int quantity) {
        totalSales[productIndex] += quantity;
    }



}