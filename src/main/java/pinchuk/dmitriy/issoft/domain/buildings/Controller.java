package pinchuk.dmitriy.issoft.domain.buildings;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class Controller implements Runnable {

    @Getter
    private volatile boolean isWorking;
    @Setter
    @Getter
    private List<Elevator> elevators;
    @Getter
    private final Queue<Trip> trips;

    private final Condition controllerStopCondition;
    private final Lock callLock;
    private final Lock elevatorLock;

    public Controller() {

        this.elevators = new ArrayList<>();
        this.trips = new LinkedList<>();
        this.callLock = new ReentrantLock(true);
        this.elevatorLock = new ReentrantLock(true);
        this.controllerStopCondition = callLock.newCondition();
        this.isWorking = false;

    }

    public static Controller of(List<Elevator> elevators) {
        checkNotNull(elevators);

        Controller controller = new Controller();
        controller.setElevators(elevators);

        return controller;
    }

    public List<Trip> getAllTrips() {
        callLock.lock();
        List<Trip> allTrips = ImmutableList.copyOf(trips);
        callLock.unlock();

        return allTrips;
    }

    public boolean canCallElevator(Trip trip) {
        checkNotNull(trip);

        elevatorLock.lock();
        boolean result = elevators.stream()
                .noneMatch(i -> (i.getDirectionOfTravel().equals(trip.getDirection()) || i.getDirectionOfTravel().equals(DirectionOfTravel.NONE))
                        && i.getCurrentFloorNumber() == trip.getTargetFloorWithIndexNumber()
                        && (i.getElevatorState().equals(ElevatorState.LOAD) || i.getElevatorState().equals(ElevatorState.OPEN_DOOR)));
        elevatorLock.unlock();

        return result;

    }

    public void addTrip(Trip trip) {
        checkNotNull(trip);
        checkArgument(trip.getTargetFloorWithIndexNumber() >= Floor.NUMBER_OF_FIRST_FLOOR);

        callLock.lock();
        trips.add(trip);
        controllerStopCondition.signal();
        callLock.unlock();

        log.info("trip added: {}", trip.getTargetFloorWithIndexNumber());
    }

    public void removeTrip(Trip trip) {
        checkNotNull(trip);

        callLock.lock();
        trips.removeAll(trips.stream().filter(trip::equals).collect(Collectors.toList()));
        callLock.unlock();
        log.info("trip has been removed {}", trip);
    }

    @SneakyThrows
    public void waitTrip() {
        callLock.lock();
        while (trips.isEmpty()) {
            controllerStopCondition.await();
        }
        callLock.unlock();
    }

    public void sendTrip() {
        callLock.lock();

        if (!trips.isEmpty()) {
            Trip trip = trips.poll();

            List<Elevator> suitableElevators;

            elevatorLock.lock();
            suitableElevators = elevators.stream()
                    .filter(i -> i.getDirectionOfTravel().equals(DirectionOfTravel.NONE)
                            && i.getElevatorState().equals(ElevatorState.STOP))
                    .sorted(Comparator.comparing(i -> Math.abs(i.getCurrentFloorNumber() - trip.getTargetFloorWithIndexNumber())))
                    .collect(Collectors.toList());
            elevatorLock.unlock();

            if (!suitableElevators.isEmpty()) {
                suitableElevators.get(0).addTrip(trip);
            } else {
                trips.add(trip);
            }

        }

        callLock.unlock();
    }

    @Override
    public void run() {

        unpause();

        while (isWorking) {
            while (trips.isEmpty()) {
                waitTrip();
            }
            sendTrip();
        }
    }


    public void pause() {
        isWorking = false;
    }

    public void unpause() {
        isWorking = true;
    }
}
