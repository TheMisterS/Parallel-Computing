import java.util.Random;
/*
Lab work 2 for parallel computing course
It simulates an elevator that is able to only go one direction and hold up to N amount of people at the same time.
One class - elevator, implements the synchronization logic, while passenger object/s use it.


5. "Rytų - Vakarų" gijų veikos sinchronizatorius. -
klasikinės  "skaitytojų-rašytojų" problemos variacija.
Vienu metu leidžiama būti "aktyvioms" tik vienos krypties gijoms,
bet ne daugiau negu N - programos parametras. Palyginimui situacija -
vienos eiles automobiliu tiltas, kuriuo mašinos gali važiuoti
viena kryptimi.

Author: Simonas Jaunius Urbutis

 */
public class Main {
    public static void main(String[] args){
        int maxCars = 3;
        Elevator elevator = new Elevator(maxCars);
        Random rand = new Random();

        String[] directions = {"UP", "DOWN"};
        // RANDOM TEST
        for(int i = 0; i < 10; i++){
            new Passenger(elevator, directions[rand.nextInt(2)]).start();
        }
    }
}