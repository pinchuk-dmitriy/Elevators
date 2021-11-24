package pinchuk.dmitriy.issoft.domain.buildings;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pinchuk.dmitriy.issoft.domain.util.PeopleSpawn;
import pinchuk.dmitriy.issoft.domain.people.Person;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Getter
public class Floor {

    public static int NUMBER_OF_FIRST_FLOOR = 0;

    private final int numberOfFloor;
    private final Building building;
    private boolean upButtonPressed = false;
    private boolean downButtonPressed = false;

    @Setter
    private PeopleSpawn peopleSpawn;

    private Queue<Person> upQueue;
    private Queue<Person> downQueue;

    private final Lock floorLock;
    public static Floor of(int numberOfFloor, Building building) {
        checkArgument(numberOfFloor >= NUMBER_OF_FIRST_FLOOR);
        checkArgument(numberOfFloor < building.getNumberOfFloors());
        checkNotNull(building);

        Floor floor = new Floor(numberOfFloor, building);
        floor.setPeopleSpawn(PeopleSpawn.of(floor));
        return floor;
    }

    private Floor(int numberOfFloor, Building building) {
        this.numberOfFloor = numberOfFloor;

        if(building.getNumberOfFloors() != numberOfFloor) {
            this.upQueue = new LinkedList<>();
        }

        if(building.getNumberOfFloors() != NUMBER_OF_FIRST_FLOOR) {
            this.downQueue = new LinkedList<>();
        }

        this.building = building;
        this.floorLock = new ReentrantLock(true);
    }

    public void addPerson(Person person) {
        checkNotNull(person);

        floorLock.lock();
        DirectionOfTravel direction = person.getTrip().getDirection();
        if (direction == DirectionOfTravel.UP) {

            if(getUpQueue().isEmpty()) {
                person.pushButton();
            }

            getUpQueue().add(person);
        } else if (direction == DirectionOfTravel.DOWN) {

            if(getDownQueue().isEmpty()) {
                person.pushButton();
            }

            getDownQueue().add(person);
        }

        floorLock.unlock();

        log.info("person has been added to {}", person);
    }

    public int getNumberOfPeople(DirectionOfTravel direction) {
        checkNotNull(direction);

        floorLock.lock();
        direction = getDirectionOfTravel(direction);
        int count = direction.equals(DirectionOfTravel.UP) ? upQueue.size() : downQueue.size();
        floorLock.unlock();

        return count;
    }

    public void callElevator(DirectionOfTravel direction) {
        this.building.getController().addTrip(Trip.of(numberOfFloor, direction));
    }

    private DirectionOfTravel getDirectionOfTravel(DirectionOfTravel direction){

        if (direction == DirectionOfTravel.NONE) {
            return upQueue.size() > downQueue.size() ? DirectionOfTravel.UP : DirectionOfTravel.DOWN;
        }

        return direction;
    }

    public Person getFirstPerson(DirectionOfTravel direction) {

        floorLock.lock();
        direction = getDirectionOfTravel(direction);
        Person person = direction.equals(DirectionOfTravel.UP) ? upQueue.peek() : downQueue.peek();
        floorLock.unlock();

        return person;
    }

    public Person pollFirstPerson(DirectionOfTravel direction) {

        Person person = null;

        floorLock.lock();
        direction = getDirectionOfTravel(direction);
        if (getFirstPerson(direction) != null) {
            person = direction.equals(DirectionOfTravel.UP) ? upQueue.poll() : downQueue.poll();

            if (getFirstPerson(direction) != null
                    && building.getController().canCallElevator(getFirstPerson(direction).getTrip())) {
                callElevator(direction);
            }

            log.info("person has been polled {}", person);
        }
        floorLock.unlock();

        return person;
    }
}
