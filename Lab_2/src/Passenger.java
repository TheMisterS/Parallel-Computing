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
            System.out.println("[Passenger]: AFTER ENTERING ELEVATOR IN DIRECTION: " + direction);
//            if(elevator.getCurrentPassengers() > elevator.getMaxPassengers()){
//                System.out.println("BADBADBAD");
//            }
//            if (elevator.getCurrentPassengers() > 1 && !elevator.getCurrentDirection().equals(direction)) {
//                System.out.println("WRONG DIRECTION ENTRY!");
//            }
            // Sleep to imitate time spent on the elevator before exiting
            Thread.sleep(5);
            System.out.println("[Passenger]: BEFORE LEAVING ELEVATOR, FORMER DIRECTION: "  + direction);
            elevator.exitElevator();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}