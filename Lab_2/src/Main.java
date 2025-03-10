import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int maxCars = 3;
        Elevator elevator = new Elevator(maxCars);
        Random rand = new Random();

        String[] directions = {"UP", "DOWN"};
        // RANDOM TEST
        for(int i = 0; i < 1000; i++){
            new Passenger(elevator, directions[rand.nextInt(2)]).start();
            new Passenger(elevator, directions[rand.nextInt(2)]).start();
            new Passenger(elevator, directions[rand.nextInt(2)]).start();
        }
    }
}