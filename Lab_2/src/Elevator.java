public class Elevator {
    private int maxPassengers;
    private int currentPassengers;
    private String currentDirection = "NONE";

    public Elevator(int maxPassengers) {
        this.maxPassengers = maxPassengers;
        this.currentPassengers = 0;
    }

    public int getMaxPassengers() {
        return maxPassengers;
    }

    public void setMaxPassengers(int maxPassengers) {
        this.maxPassengers = maxPassengers;
    }

    public int getCurrentPassengers() {
        return currentPassengers;
    }

    public void setCurrentPassengers(int currentPassengers) {
        this.currentPassengers = currentPassengers;
    }

    public String getCurrentDirection() {
        return currentDirection;
    }

    public void setCurrentDirection(String currentDirection) {
        this.currentDirection = currentDirection;
    }

    public synchronized void enterElevator(String direction) throws InterruptedException{
        // Semafore function P(S)
        while ((currentPassengers > 0 && !currentDirection.equals(direction)) || currentPassengers >= maxPassengers) {
            wait();
        }
        currentDirection = direction;
        currentPassengers++;
        // System.out.println("[ELEVATOR]:Passenger entering elevator in direction: " + direction + ", Passenger count inside elevator: " + currentPassengers);
    }

    // Semafore function V(S)
    public synchronized void exitElevator(){
        currentPassengers--;
        //System.out.println("[ELEVATOR]:Passenger exiting elevator, Remaining: " + currentPassengers);
        if (currentPassengers == 0){
            currentDirection = "NONE";
            // System.out.println("[ELEVATOR]: ELEVATOR IS EMPTY!");
        }
        notifyAll();
    }
}

