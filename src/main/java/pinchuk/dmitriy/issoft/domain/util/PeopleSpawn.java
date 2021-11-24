package pinchuk.dmitriy.issoft.domain.util;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import pinchuk.dmitriy.issoft.domain.buildings.DirectionOfTravel;
import pinchuk.dmitriy.issoft.domain.buildings.Floor;
import pinchuk.dmitriy.issoft.domain.people.Person;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Getter
public class PeopleSpawn extends Thread {

   private static int MIN_WEIGHT_OF_PERSON = 10;
    private static int MAX_WEIGHT_OF_PERSON = 180;
    private static int TIMEOUT_BETWEEN_GENERATE_PEOPLE = 20;
    private static int DEFAULT_INTENSITY = 1;

    private int intensity;
    private final Floor floor;
    private boolean isWorking = true;

    private final Random random;
    public static PeopleSpawn of(Floor floor) {
        return new PeopleSpawn(floor);
    }

    private PeopleSpawn(Floor floor) {
        checkNotNull(floor);

        this.intensity = DEFAULT_INTENSITY;
        this.floor = floor;
        this.random = new Random();

    }

    @Override
    public void run() {

        unpause();


        while(isWorking) {
            generatePeople();
        }

    }

    @SneakyThrows
    public void generatePeople() {

        for (int i = 1; i <= intensity; i++) {

            int randomFloorNumber = randomNeededFloor();

            if (this.floor.getNumberOfFloor() != randomFloorNumber) {
                addPerson(randomFloorNumber);
            }

        }

        TimeUnit.SECONDS.sleep(TIMEOUT_BETWEEN_GENERATE_PEOPLE);
    }

    public void addPerson(int randomFloorNumber) {

        Person person = Person.of(randomWeight(), this.floor, randomFloorNumber);
        DirectionOfTravel direction = person.getTrip().getDirection();

        floor.getFloorLock().lock();

        if (direction == DirectionOfTravel.DOWN) {

            if(floor.getDownQueue().isEmpty()) {
                person.pushButton();
            }

            this.floor.getDownQueue().add(person);
        }

        if (direction == DirectionOfTravel.UP) {

            if(floor.getUpQueue().isEmpty()) {
                person.pushButton();
            }

            this.floor.getUpQueue().add(person);
        }

        Storage.getInstance().incrementNumberOfGeneratedPeople();

        floor.getFloorLock().unlock();
    }

    public void pause() {
        isWorking = false;
    }

    public void unpause() {
        isWorking = true;
    }

    private int randomWeight() {
        return Math.abs(random.nextInt()) % (MAX_WEIGHT_OF_PERSON - MIN_WEIGHT_OF_PERSON) + MIN_WEIGHT_OF_PERSON;
    }

    private int randomNeededFloor() {
        return (int) (Math.random() * (floor.getBuilding().getNumberOfFloors()-1));
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

}
