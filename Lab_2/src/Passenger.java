class Passenger extends Thread {
    private Elevator elevator;
    private String direction;

    public Passenger(Elevator elevator, String direction) {
        this.elevator = elevator;
        this.direction = direction;
    }

    @Override
    public void run() {
        try {
            elevator.enterElevator(direction);
            Thread.sleep(5);
            elevator.exitElevator();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}