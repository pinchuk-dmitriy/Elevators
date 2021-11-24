package pinchuk.dmitriy.issoft.domain.buildings;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Slf4j
public class Building {

    private int id;
    private final int numberOfFloors;
    private final int numberOfElevators;
    private final String address;

    private List<Floor> floors;
    private List<Elevator> elevators;

    private Lock buildingLock;

    @Getter
    private volatile Controller controller;

    public static Building of(int id, int numberOfFloors, int numberOfElevators, String address) {
        return new Building(id, numberOfFloors, numberOfElevators, address);
    }

    private Building(int id, int numberOfFloors, int numberOfElevators, String address) {
        checkArgument(numberOfFloors > 0);
        checkArgument(numberOfFloors > 0);
        checkNotNull(address);

        this.id = id;
        this.numberOfFloors = numberOfFloors;
        this.numberOfElevators = numberOfElevators;
        this.address = address;
        this.floors = new ArrayList<>();
        this.elevators = new ArrayList<>();
        this.buildingLock = new ReentrantLock(true);

        IntStream.range(0 , numberOfFloors).forEachOrdered(i -> getFloors().add(Floor.of(i, this)));
        IntStream.range(0 , numberOfElevators).forEachOrdered(i -> getElevators().add(Elevator.of(i, this)));
    }

    public Building setController(Controller controller) {
        checkNotNull(controller);

        this.controller = controller;
        controller.setElevators(elevators);

        return this;
    }

    public Floor getFloorWithIndex(int index) {
        checkArgument(index < numberOfFloors);

        return floors.get(index);
    }

    public void startAllPeopleSpawn() {
        IntStream.range( 0, this.numberOfFloors).forEachOrdered(i -> new Thread(floors.get(i).getPeopleSpawn(), "PeopleSpawnGenerator " + i).start());
    }

    public void startAllElevators() {
        IntStream.range( 0, this.numberOfElevators).forEachOrdered(i -> new Thread(elevators.get(i), "Elevator " + i).start());
    }

    public void startController() {
        checkNotNull(controller);
        new Thread(controller, "Controller").start();
    }

    public void stop() {
        checkNotNull(controller);

        stopElevators();
        stopController();
    }

    public void stopController() {
        checkNotNull(controller);
        controller.pause();
    }

    public void stopElevators() {
        checkNotNull(controller);
        elevators.forEach(Elevator::pause);
    }
}
