package pinchuk.dmitriy.issoft.buildings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.domain.buildings.*;
import pinchuk.dmitriy.issoft.domain.people.Person;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ElevatorTest {

    public static Building building;

    @BeforeEach
    void init() {
        building = BuildingSamples.anyValidBuilding();
    }

    @Test
    void createValidElevator() {
        Elevator elevator = Elevator.of(100, building);
        elevator.setLiftingCapacity(100);

        assertTrue(elevator.getLiftingCapacity() == 100);
        assertTrue(elevator.getTravelSpeed() == Elevator.DEFAULT_ELEVATOR_TRAVEL_SPEED);
        assertTrue(elevator.getDoorSpeed() == Elevator.DEFAULT_DOOR_SPEED);
        assertTrue(elevator.getCurrentFloorNumber() == Floor.NUMBER_OF_FIRST_FLOOR);
    }

    @Test
    void createValidElevatorWithStartFloor() {
        Elevator elevator = Elevator.of(100, building);
        elevator.setCurrentFloorNumber(new AtomicInteger(1));

        assertTrue(elevator.getCurrentFloorNumber() == 1);
        assertTrue(elevator.getTravelSpeed() == Elevator.DEFAULT_ELEVATOR_TRAVEL_SPEED);
        assertTrue(elevator.getDoorSpeed() == Elevator.DEFAULT_DOOR_SPEED);
        assertTrue(elevator.getCurrentFloorNumber() == 1);
    }

    @Test
    void createValidElevatorWithStartSpeed() {
        Elevator elevator = Elevator.of(100, building);
        elevator.setDoorSpeed(2);
        elevator.setTravelSpeed(2);

        assertTrue(elevator.getDoorSpeed() == 2);
        assertTrue(elevator.getTravelSpeed() == 2);
    }

    @Test
    void goUp() {
        building.getElevators().get(0).goUp();

        assertTrue(building.getElevators().get(0).getDirectionOfTravel() == DirectionOfTravel.UP);
        assertTrue(building.getElevators().get(0).getElevatorState() == ElevatorState.MOVE);
    }

    @Test
    void goDown() {
        building.getElevators().get(0).goUp();
        building.getElevators().get(0).goDown();

        assertTrue(building.getElevators().get(0).getDirectionOfTravel() == DirectionOfTravel.DOWN);
        assertTrue(building.getElevators().get(0).getElevatorState() == ElevatorState.MOVE);
    }

    @Test
    void openDoor() {
        building.getElevators().get(0).openDoor();

        assertTrue(building.getElevators().get(0).getElevatorState() == ElevatorState.OPEN_DOOR);
    }

    @Test
    void closeDoor() {
        building.getElevators().get(0).closeDoor();

        assertTrue(building.getElevators().get(0).getElevatorState() == ElevatorState.CLOSE_DOOR);
    }

    @Test
    void end() {
        building.getElevators().get(0).end();

        assertTrue(building.getElevators().get(0).getDirectionOfTravel() == DirectionOfTravel.NONE);
        assertTrue(building.getElevators().get(0).getElevatorState() == ElevatorState.END);
    }

    @Test
    void takePersonWithMoveUpState() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        Person person = Person.of(50, building.getFloorWithIndex(1), 5);

        building.getFloorWithIndex(1).addPerson(person);

        building.getElevators().get(0).goUp();
        building.getElevators().get(0).takePerson(person);

        assertTrue(building.getElevators().get(0).getPassengers().contains(person));
        assertTrue(building.getElevators().get(0).getDirectionOfTravel() == person.getTrip().getDirection());
    }

    @Test
    void takePersonWithMoveDownState() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setCurrentFloorNumber(new AtomicInteger(5));
        Person person = Person.of(50, building.getFloorWithIndex(5), 1);

        building.getFloors().get(5).addPerson(person);

        building.getElevators().get(0).goDown();
        building.getElevators().get(0).takePerson(person);

        assertTrue(building.getElevators().get(0).getPassengers().contains(person));
        assertTrue(building.getElevators().get(0).getDirectionOfTravel() == person.getTrip().getDirection());
    }

    @Test
    void takeNullPerson() {
        building = Building.of(5, 10, 1, "address");

        assertThrows(NullPointerException.class, () -> building.getElevators().get(0).takePerson(null));
    }

    @Test
    void letPeopleGo() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());

        Person person = Person.of(50, building.getFloorWithIndex(1), 5);

        building.getFloors().get(1).addPerson(person);

        building.getElevators().get(0).takePerson(person);
        building.getElevators().get(0).letPeopleGo(person);

        assertFalse(building.getElevators().get(0).getPassengers().contains(person));
    }

    @Test
    void letGoNullPerson() {
        building = Building.of(5, 10, 1, "address");

        assertThrows(NullPointerException.class, () -> building.getElevators().get(0).letPeopleGo(null));
    }

    @Test
    void removeExecutedTrips(){
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setCurrentFloorNumber(new AtomicInteger(1));

        Trip trip = Trip.of(1, DirectionOfTravel.UP);

        building.getController().addTrip(trip);
        building.getController().sendTrip();

        assertTrue(building.getElevators().get(0).getTrips().contains(trip));
        assertTrue(building.getElevators().get(0).removeExecutedTrips());
        assertFalse(building.getElevators().get(0).getTrips().contains(trip));
    }

    @Test
    void addPeopleWithNoSpace() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setLiftingCapacity(100);

        Person firstPerson = Person.of(60, building.getFloorWithIndex(1), 5);
        Person secondPerson = Person.of(60, building.getFloorWithIndex(1), 5);

        building.getFloors().get(1).addPerson(firstPerson);
        building.getFloors().get(1).addPerson(secondPerson);

        building.getElevators().get(0).goUp();
        building.getElevators().get(0).addPeople();

        assertTrue(building.getElevators().get(0).getPassengers().contains(firstPerson));
        assertFalse(building.getElevators().get(0).getPassengers().contains(secondPerson));
        assertTrue(building.getController().getAllTrips().contains(Trip.of(1, firstPerson.getTrip().getDirection())));
    }

    @Test
    void checkFloorWithPickingUp() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setLiftingCapacity(100);
        building.getElevators().get(0).addTrip(Trip.of(5, DirectionOfTravel.UP));

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 5);

        building.getFloors().get(1).addPerson(firstPerson);

        building.getElevators().get(0).goUp();

        assertTrue(building.getElevators().get(0).checkCurrentFloor());
    }


    @Test
    void checkFloorWithoutPickingUp() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setLiftingCapacity(100);
        building.getElevators().get(0).addTrip(Trip.of(5, DirectionOfTravel.UP));

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 5);

        building.getFloors().get(1).addPerson(firstPerson);

        building.getElevators().get(0).goUp();

        assertTrue(building.getElevators().get(0).checkCurrentFloor());
    }

    @Test
    void addPeople() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setLiftingCapacity(100);

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 5);

        building.getFloorWithIndex(1).addPerson(firstPerson);

        building.getElevators().get(0).goUp();
        building.getElevators().get(0).addPeople();

        assertTrue(building.getElevators().get(0).getPassengers().contains(firstPerson));
    }

    @Test
    void loadWithoutPickingUp() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setCurrentFloorNumber(new AtomicInteger(5));
        building.getElevators().get(0).setLiftingCapacity(300);

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 5);

        building.getFloors().get(1).addPerson(firstPerson);
        building.getElevators().get(0).addTrip(Trip.of(5, 1));

        building.getElevators().get(0).goUp();
        building.getElevators().get(0).addPeople();

        assertFalse(building.getElevators().get(0).getPassengers().contains(firstPerson));
    }

    @Test
    void loadWithPickingUp() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setLiftingCapacity(100);

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 5);

        building.getFloors().get(1).addPerson(firstPerson);

        building.getElevators().get(0).addTrip(Trip.of(5, 1));

        building.getElevators().get(0).goUp();
        building.getElevators().get(0).addPeople();

        assertTrue(building.getElevators().get(0).getPassengers().contains(firstPerson));
    }

    @Test
    void loadFromEmptyFloor() {
        building = Building.of(5, 10, 1, "address");
        building.getElevators().get(0).setCurrentFloorNumber(new AtomicInteger(1));
        building.getElevators().get(0).setLiftingCapacity(100);

        building.getElevators().get(0).addPeople();

        assertTrue(building.getElevators().get(0).getPassengers().isEmpty());
    }

    @Test
    void loadFromTheLongestQueue() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setCurrentFloorNumber(new AtomicInteger(1));
        building.getElevators().get(0).setLiftingCapacity(200);

        Person firstPerson = Person.of(50, building.getFloorWithIndex(2), 1);
        Person secondPerson = Person.of(50, building.getFloorWithIndex(2), 1);
        Person thirdPerson = Person.of(50, building.getFloorWithIndex(0), 1);

        building.getFloors().get(1).addPerson(firstPerson);
        building.getFloors().get(1).addPerson(secondPerson);
        building.getFloors().get(1).addPerson(thirdPerson);

        building.getElevators().get(0).addPeople();

        assertTrue(building.getElevators().get(0).getPassengers().contains(firstPerson));
        assertTrue(building.getElevators().get(0).getPassengers().contains(secondPerson));
        assertFalse(building.getElevators().get(0).getPassengers().contains(thirdPerson));
    }

    @Test
    void loadFromUpQueue() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setLiftingCapacity(200);

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 2);
        Person secondPerson = Person.of(50, building.getFloorWithIndex(1), 2);
        Person thirdPerson = Person.of(50, building.getFloorWithIndex(1), 0);

        building.getFloors().get(1).addPerson(firstPerson);
        building.getFloors().get(1).addPerson(secondPerson);
        building.getFloors().get(1).addPerson(thirdPerson);

        building.getElevators().get(0).goUp();
        building.getElevators().get(0).addTrip(Trip.of(2, 1));
        building.getElevators().get(0).addPeople();

        assertTrue(building.getElevators().get(0).getPassengers().contains(firstPerson));
        assertTrue(building.getElevators().get(0).getPassengers().contains(secondPerson));
        assertFalse(building.getElevators().get(0).getPassengers().contains(thirdPerson));
    }

    @Test
    void loadFromDownQueue() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setCurrentFloorNumber(new AtomicInteger(2));
        building.getElevators().get(0).setLiftingCapacity(200);

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 2);
        Person secondPerson = Person.of(50, building.getFloorWithIndex(1), 2);
        Person thirdPerson = Person.of(50, building.getFloorWithIndex(1), 0);

        building.getFloors().get(1).addPerson(firstPerson);
        building.getFloors().get(1).addPerson(secondPerson);
        building.getFloors().get(1).addPerson(thirdPerson);

        building.getElevators().get(0).goDown();
        building.getElevators().get(0).addTrip(Trip.of(0, 1));
        building.getElevators().get(0).addPeople();

        assertFalse(building.getElevators().get(0).getPassengers().contains(firstPerson));
        assertFalse(building.getElevators().get(0).getPassengers().contains(secondPerson));
        assertTrue(building.getElevators().get(0).getPassengers().contains(thirdPerson));
    }

    @Test
    void loadFromUpQueueWithUpDestinationDirectionTest() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setCurrentFloorNumber(new AtomicInteger(1));
        building.getElevators().get(0).setLiftingCapacity(200);

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 2);
        Person secondPerson = Person.of(50, building.getFloorWithIndex(1), 2);
        Person thirdPerson = Person.of(50, building.getFloorWithIndex(1), 0);

        building.getFloors().get(1).addPerson(firstPerson);
        building.getFloors().get(1).addPerson(secondPerson);
        building.getFloors().get(1).addPerson(thirdPerson);

        building.getElevators().get(0).addTrip(Trip.of(2, 1));
        building.getElevators().get(0).addPeople();

        assertTrue(building.getElevators().get(0).getPassengers().contains(firstPerson));
        assertTrue(building.getElevators().get(0).getPassengers().contains(secondPerson));
    }

    @Test
    void loadFromDownQueueWithDownDestinationDirection() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());
        building.getElevators().get(0).setCurrentFloorNumber(new AtomicInteger(1));
        building.getElevators().get(0).setLiftingCapacity(200);

        Person firstPerson = Person.of(50, building.getFloorWithIndex(1), 2);
        Person secondPerson = Person.of(50, building.getFloorWithIndex(1), 2);
        Person thirdPerson = Person.of(50, building.getFloorWithIndex(1), 0);

        building.getFloors().get(1).addPerson(firstPerson);
        building.getFloors().get(1).addPerson(secondPerson);
        building.getFloors().get(1).addPerson(thirdPerson);

        building.getElevators().get(0).addTrip(Trip.of(0, 1));
        building.getElevators().get(0).addPeople();

        assertTrue(building.getElevators().get(0).getPassengers().contains(thirdPerson));
    }

    @Test
    void unpause() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());

        building.getElevators().get(0).unpause();

        assertTrue(building.getElevators().get(0).isMoving());
    }

    @Test
    void pause() {
        building = Building.of(5, 10, 1, "address").setController(new Controller());

        building.getElevators().get(0).unpause();
        building.getElevators().get(0).pause();

        assertFalse(building.getElevators().get(0).isMoving());
    }
}
