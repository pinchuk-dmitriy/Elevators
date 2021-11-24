package pinchuk.dmitriy.issoft.domain.buildings;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import pinchuk.dmitriy.issoft.domain.people.Person;
import pinchuk.dmitriy.issoft.domain.util.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;

@Slf4j
@Getter
@Setter
public class Elevator extends Thread {

    public static int DEFAULT_DOOR_SPEED = 1;
    public static int DEFAULT_ELEVATOR_CAPACITY = 400;
    public static int DEFAULT_ELEVATOR_TRAVEL_SPEED = 1;
    public static int START_FLOOR_NUMBER = 0;

    private final int number;
    private int liftingCapacity;
    private int travelSpeed; //floors in second
    private int doorSpeed;
    private volatile boolean isMoving;

    private volatile Building building;
    private volatile DirectionOfTravel directionOfTravel;
    private volatile ElevatorState elevatorState;

    private final AtomicInteger numberOfDeliveredPeople;
    private AtomicInteger currentFloorNumber;
    private List<Person> passengers = new ArrayList<>();
    private List<Trip> trips;

    private Condition elevatorStopCondition;
    private final Lock elevatorLock;
    private final Lock stateLock;
    private final Lock tripLock;
    private final Lock currentFloorLock;
    private final Lock printLock;

    public static Elevator of(int number, Building building) {
        return new Elevator(number, DEFAULT_ELEVATOR_CAPACITY, DEFAULT_ELEVATOR_TRAVEL_SPEED, building);
    }

    public static Elevator of(int number, int liftingCapacity, Building building) {
        return new Elevator(number, liftingCapacity, DEFAULT_ELEVATOR_TRAVEL_SPEED, building);
    }

    public static Elevator of(int number, int liftingCapacity, int travelSpeed, Building building) {
        return new Elevator(number, liftingCapacity, travelSpeed, building);
    }

    private Elevator(int number, int liftingCapacity, int travelSpeed, Building building) {

        this.number = number;
        this.liftingCapacity = liftingCapacity;
        this.travelSpeed = travelSpeed;
        this.building = building;
        this.doorSpeed = DEFAULT_DOOR_SPEED;
        this.directionOfTravel = DirectionOfTravel.NONE;
        this.currentFloorNumber = new AtomicInteger(START_FLOOR_NUMBER);
        this.numberOfDeliveredPeople = new AtomicInteger(0);
        this.trips = new ArrayList<>();
        this.elevatorState = ElevatorState.STOP;
        this.elevatorLock = new ReentrantLock();
        this.stateLock = new ReentrantLock();
        this.tripLock = new ReentrantLock();
        this.currentFloorLock = new ReentrantLock();
        this.printLock = new ReentrantLock();
        this.elevatorStopCondition = tripLock.newCondition();

    }


    public int getCurrentFloorNumber() {
        currentFloorLock.lock();
        int floor = currentFloorNumber.get();
        currentFloorLock.unlock();

        return floor;
    }

    public Floor getCurrentFloor() {
        currentFloorLock.lock();
        Floor floor = building.getFloorWithIndex(currentFloorNumber.get());
        currentFloorLock.unlock();

        return floor;
    }

    public int getFreeCapacity() {
        tripLock.lock();
        int usedSpace = passengers.stream().mapToInt(Person::getWeight).sum();
        tripLock.unlock();

        return liftingCapacity - usedSpace;
    }

    public DirectionOfTravel getDirectionOfTravel() {
        stateLock.lock();
        DirectionOfTravel direction = this.directionOfTravel;
        stateLock.unlock();

        return direction;
    }

    public ElevatorState getElevatorState() {
        stateLock.lock();
        ElevatorState state = this.elevatorState;
        stateLock.unlock();

        return state;
    }

    private DirectionOfTravel getNextDirection() {
        tripLock.lock();
        stateLock.lock();
        DirectionOfTravel currentDirection;

        if(currentFloorNumber.get() == building.getNumberOfFloors()) {
            currentDirection = DirectionOfTravel.DOWN;
        } else {
            currentDirection = trips.isEmpty() ? DirectionOfTravel.NONE : trips.get(0).getDirection();
        }

        stateLock.unlock();
        tripLock.unlock();
        return currentDirection;
    }

    public void addTrip(Trip trip) {
        checkNotNull(trip);

        tripLock.lock();
        trips.add(trip);
        elevatorStopCondition.signal();
        tripLock.unlock();

        stateLock.lock();
        currentFloorLock.lock();

        if (directionOfTravel == DirectionOfTravel.NONE) {
            directionOfTravel = trip.getTargetFloorWithIndexNumber() - currentFloorNumber.get() > 0
                    ? DirectionOfTravel.UP : DirectionOfTravel.DOWN;
        }

        currentFloorLock.unlock();
        stateLock.unlock();

        log.info("elevator called from " + currentFloorNumber.get() + " with direction " + directionOfTravel + " to " + trip);
    }

    @SneakyThrows
    private void disable() {

        tripLock.lock();
        stateLock.lock();
        directionOfTravel = DirectionOfTravel.NONE;
        elevatorState = ElevatorState.STOP;
        stateLock.unlock();

        while (trips.isEmpty()) {
            log.info("elevator " + number + " stopped");
            elevatorStopCondition.await();
        }

        tripLock.unlock();
    }

    public void end() {
        stateLock.lock();
        directionOfTravel = DirectionOfTravel.NONE;
        elevatorState = ElevatorState.END;
        stateLock.unlock();

        log.warn("elevator has finished his way");
    }

    public boolean checkCurrentFloor() {
        Person person = null;
        boolean result;

        getCurrentFloor().getFloorLock().lock();
        stateLock.lock();
        DirectionOfTravel destinationDirection = getNextDirection();

        if (!destinationDirection.equals(DirectionOfTravel.NONE) && destinationDirection.equals(directionOfTravel)) {
            person = getCurrentFloor().getFirstPerson(directionOfTravel);
        }

        stateLock.unlock();
        getCurrentFloor().getFloorLock().unlock();

        result = checkPerson(person);

        return result;
    }

    private boolean checkPerson(Person person) {
        elevatorLock.lock();
        boolean result = false;

        if (person != null && person.getWeight() <= getFreeCapacity()) {
            stateLock.lock();
            result = person.getTrip().getDirection() == directionOfTravel;
            stateLock.unlock();
        }

        elevatorLock.unlock();

        return result;
    }

    @SneakyThrows
    public void openDoor() {
        stateLock.lock();
        elevatorState = ElevatorState.OPEN_DOOR;
        stateLock.unlock();

        TimeUnit.SECONDS.sleep(doorSpeed);
        log.info("elevator has opened his door");
    }

    @SneakyThrows
    public void closeDoor() {
        stateLock.lock();
        this.elevatorState = ElevatorState.CLOSE_DOOR;
        stateLock.unlock();
        TimeUnit.SECONDS.sleep(getDoorSpeed());

        log.info("elevator has closed his door");
    }

    public void addPeople() {

        stateLock.lock();
        this.elevatorState = ElevatorState.LOAD;
        stateLock.unlock();

        elevatorLock.lock();
        List<Person> peopleForDisembark = passengers.stream()
                .filter(i -> i.getTrip().getTargetFloorWithIndexNumber() == currentFloorNumber.get())
                .collect(Collectors.toList());
        elevatorLock.unlock();

        peopleForDisembark.forEach(this::letPeopleGo);
        log.info("elevator has finished disembarking");

        elevatorLock.lock();
        stateLock.lock();
        if (passengers.isEmpty() && trips.isEmpty()) {
            log.info("elevator is empty");
            directionOfTravel = DirectionOfTravel.NONE;
        } else if (passengers.isEmpty()) {
            directionOfTravel = getNextDirection();
        }
        stateLock.unlock();
        elevatorLock.unlock();

        loadPeople();

        log.info("elevator finishes load");
    }

    public void letPeopleGo(Person person) {
        checkNotNull(person);
        checkArgument(passengers.contains(person));

        elevatorLock.lock();
        passengers.remove(person);
        elevatorLock.unlock();

        Storage.getInstance().incrementNumberOfDeliveredPeople();
        numberOfDeliveredPeople.incrementAndGet();
        log.info("elevator disembark the next Person: {}", person);
    }

    @SneakyThrows
    public void takePerson(Person person) {
        checkNotNull(person);

        stateLock.lock();
        if (directionOfTravel == DirectionOfTravel.NONE) {
            directionOfTravel = person.getTrip().getDirection();
        }
        stateLock.unlock();

        elevatorLock.lock();
        passengers.add(person);
        elevatorLock.unlock();

        building.getController().removeTrip(Trip.of(getCurrentFloorNumber(), person.getTrip().getDirection()));
        addTrip(person.getTrip());

        TimeUnit.SECONDS.sleep(doorSpeed);

        log.info("elevator take the next Person: {}", person);
    }

    @SneakyThrows
    public void loadPeople() {

        boolean isLoading = true;
        while (elevatorState == ElevatorState.LOAD && isLoading) {
            getCurrentFloor().getFloorLock().lock();
            stateLock.lock();

            Person person = building.getFloorWithIndex(getCurrentFloorNumber()).getFirstPerson(directionOfTravel);
            DirectionOfTravel neededDirection = getNextDirection();
            if (person != null
                    && ((!neededDirection.equals(DirectionOfTravel.NONE)
                    && neededDirection.equals(person.getTrip().getDirection()))
                    || (neededDirection.equals(DirectionOfTravel.NONE)
                    && person.getTrip().getDirection().equals(directionOfTravel))
                    || directionOfTravel.equals(DirectionOfTravel.NONE))) {

                if (person.getWeight() <= getFreeCapacity()) {
                    if (directionOfTravel.equals(DirectionOfTravel.NONE)) {
                        directionOfTravel = person.getTrip().getDirection();
                    }
                    stateLock.unlock();
                    person = building.getFloorWithIndex(currentFloorNumber.get()).pollFirstPerson(directionOfTravel);

                    getCurrentFloor().getFloorLock().unlock();
                    takePerson(person);

                    log.info("person has been picked up {}", person);
                } else {
                    stateLock.unlock();
                    getCurrentFloor().getFloorLock().unlock();
                    building.getController().addTrip(Trip.of(currentFloorNumber.get(), person.getTrip().getDirection()));

                    log.info("elevator cannot pick up person, 'cause there is not enough space {}", person);
                    log.info("elevator recall {}", person.getTrip());

                    isLoading = false;
                }
            } else {
                stateLock.unlock();
                getCurrentFloor().getFloorLock().unlock();
                isLoading = false;
            }
        }
    }

    @SneakyThrows
    public void goUp() {
        checkState(getCurrentFloorNumber() < building.getNumberOfFloors());

        stateLock.lock();
        directionOfTravel = DirectionOfTravel.UP;
        elevatorState = ElevatorState.MOVE;
        stateLock.unlock();

        currentFloorLock.lock();
        currentFloorNumber.incrementAndGet();
        currentFloorLock.unlock();

        Storage.getInstance().incrementNumberOfPassedFloors();

        TimeUnit.SECONDS.sleep(travelSpeed);

        log.info("elevator moved up to floor number {}", currentFloorNumber);
    }

    @SneakyThrows
    public void goDown() {
        checkState(currentFloorNumber.get() > Floor.NUMBER_OF_FIRST_FLOOR);

        stateLock.lock();
        directionOfTravel = DirectionOfTravel.DOWN;
        elevatorState = ElevatorState.MOVE;
        stateLock.unlock();

        currentFloorLock.lock();
        currentFloorNumber.decrementAndGet();
        currentFloorLock.unlock();

        Storage.getInstance().incrementNumberOfPassedFloors();

        TimeUnit.SECONDS.sleep(travelSpeed);

        log.info("elevator moved down to floor number {}", currentFloorNumber);
    }

    public boolean removeExecutedTrips() {
        boolean hasExecutedTrips = false;

        tripLock.lock();
        List<Trip> currentFloorTrips = trips.stream()
                .filter(i -> i.getTargetFloorWithIndexNumber() == currentFloorNumber.get())
                .collect(Collectors.toList());
        hasExecutedTrips = trips.removeAll(currentFloorTrips);
        tripLock.unlock();

        return hasExecutedTrips;
    }

    @SneakyThrows
    @Override
    public void run() {

        int currentTripFloorNumber;
        boolean hasExecutedTrips;
        boolean waitOnThisFloor;

        unpause();

        while(isMoving) {

            tripLock.lock();

            if(trips.isEmpty()) {
                tripLock.unlock();
                disable();
            } else {
                hasExecutedTrips = removeExecutedTrips();
                currentTripFloorNumber = trips.isEmpty()
                        ? currentFloorNumber.get()
                        : trips.get(0).getTargetFloorWithIndexNumber();

                tripLock.unlock();

                waitOnThisFloor = checkCurrentFloor();

                if (hasExecutedTrips || waitOnThisFloor) {
                    openDoor();
                    addPeople();
                    closeDoor();
                } else if(currentTripFloorNumber > currentFloorNumber.get()) {
                    goUp();
                } else if(currentTripFloorNumber < currentFloorNumber.get()) {
                    goDown();
                }
            }
        }

        pause();
        end();

    }

    public void pause() {
        isMoving = false;
        log.info("elevator has been stopped");
    }

    public void unpause() {
        isMoving = true;
        log.info("elevator has been started");
    }

    public void setDoorSpeed(int doorSpeed) {
        this.doorSpeed = doorSpeed;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    @Override
    public String toString() {
        printLock.lock();
        String result = "Elevator " + this.getNumber() +
                ": Current floor=" + this.currentFloorNumber.get() +
                ": Free space=" + getFreeCapacity() +
                "; Passengers size=" + this.getPassengers().size() +
                "; Number of delivered people=" + this.numberOfDeliveredPeople.get() +
                ": State=" + this.elevatorState +
                "; Direction=" + this.getDirectionOfTravel();
        printLock.unlock();
        return result;
    }
}
