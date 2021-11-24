package pinchuk.dmitriy.issoft.buildings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.buildings.samples.FloorSamples;
import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.buildings.DirectionOfTravel;
import pinchuk.dmitriy.issoft.domain.buildings.Floor;
import pinchuk.dmitriy.issoft.domain.buildings.Trip;
import pinchuk.dmitriy.issoft.domain.people.Person;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FloorTest {

    public static Building building;

    @BeforeEach
    void init() {
        building = BuildingSamples.anyValidBuilding();
    }

    @Test
    void createValidFloor() {
        Floor floor = FloorSamples.anyValidFloor();

        assertTrue(floor.getNumberOfFloor() >= Floor.NUMBER_OF_FIRST_FLOOR && floor.getNumberOfFloor() < building.getNumberOfFloors());
        assertNotNull(floor.getDownQueue());
        assertNotNull(floor.getUpQueue());
        assertNotNull(floor.getBuilding());
    }

    @Test
    void createInvalidFloor() {
        assertThrows(NullPointerException.class, FloorSamples::anyInvalidFloor);
    }

    @Test
    void pollFirstPerson() {
        Floor floor = building.getFloorWithIndex(1);
        Floor firstUpperFloor = building.getFloorWithIndex(2);
        Floor secondUpperFloor = building.getFloorWithIndex(3);

        Person firstPerson = Person.of(50, firstUpperFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, secondUpperFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);

        DirectionOfTravel direction = firstPerson.getTrip().getDirection();

        assertTrue(floor.getDownQueue().contains(firstPerson));
        assertTrue(floor.pollFirstPerson(direction).equals(firstPerson));
        assertTrue(floor.getDownQueue().contains(secondPerson));
    }

    @Test
    void pollFirstPersonFromNullQueueTest() {
        Floor floor = building.getFloorWithIndex(1);

        assertDoesNotThrow(() -> floor.pollFirstPerson(DirectionOfTravel.UP));
        assertDoesNotThrow(() -> floor.pollFirstPerson(DirectionOfTravel.DOWN));
        assertDoesNotThrow(() -> floor.pollFirstPerson(DirectionOfTravel.NONE));
    }

    @Test
    void getFirstPersonFromNullQueueTest() {
        Floor floor = building.getFloorWithIndex(1);

        assertDoesNotThrow(() -> floor.getFirstPerson(DirectionOfTravel.UP));
        assertDoesNotThrow(() -> floor.getFirstPerson(DirectionOfTravel.DOWN));
        assertDoesNotThrow(() -> floor.getFirstPerson(DirectionOfTravel.NONE));
    }

    @Test
    void addPerson() {
        Floor floor = building.getFloorWithIndex(1);
        Floor firstUpperFloor = building.getFloorWithIndex(2);

        Person firstPerson = Person.of(50, firstUpperFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);

        assertFalse(floor.getDownQueue().isEmpty());
        assertTrue(floor.getDownQueue().contains(firstPerson));
        assertTrue(building.getController().getAllTrips().contains(Trip.of(firstUpperFloor.getNumberOfFloor(), firstPerson.getTrip().getDirection())));
    }

    @Test
    void addNullPerson() {
        Floor floor = building.getFloorWithIndex(1);

        assertThrows(NullPointerException.class, () -> floor.addPerson(null));
    }

    @Test
    void getFirstPerson() {
        Floor floor = building.getFloorWithIndex(1);
        Floor firstUpperFloor = building.getFloorWithIndex(2);
        Floor secondUpperFloor = building.getFloorWithIndex(3);

        Person firstPerson = Person.of(50, firstUpperFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, secondUpperFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);

        DirectionOfTravel direction = firstPerson.getTrip().getDirection();

        assertTrue(floor.getDownQueue().contains(firstPerson));
        assertTrue(floor.getFirstPerson(direction).equals(firstPerson));
        assertTrue(floor.getDownQueue().contains(secondPerson));
    }

    @Test
    void getPersonQueueWithDifferentDirectionTest() {
        Floor floor = building.getFloorWithIndex(1);
        Floor firstUpperFloor = building.getFloorWithIndex(2);

        Person firstPerson = Person.of(50, floor, firstUpperFloor.getNumberOfFloor());
        Person secondPerson = Person.of(50,  floor, firstUpperFloor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);

        assertFalse(floor.getDownQueue().contains(firstPerson));
    }

    @Test
    void getFirstPersonFromTheUpQueueTest() {
        Floor floor = building.getFloorWithIndex(1);
        Floor upperFloor = building.getFloorWithIndex(2);
        Floor lowerFloor = building.getFloorWithIndex(0);

        Person firstPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person thirdPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);
        floor.addPerson(thirdPerson);

        assertTrue(floor.getFirstPerson(DirectionOfTravel.DOWN).equals(firstPerson));
        assertTrue(floor.getFirstPerson(DirectionOfTravel.UP).equals(thirdPerson));
    }

    @Test
    void getFirstPersonFromTheDownQueue() {
        Floor floor = building.getFloorWithIndex(1);
        Floor upperFloor = building.getFloorWithIndex(2);
        Floor lowerFloor = building.getFloorWithIndex(0);

        Person firstPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, lowerFloor, upperFloor.getNumberOfFloor());
        Person thirdPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);
        floor.addPerson(thirdPerson);

        assertTrue(floor.getFirstPerson(DirectionOfTravel.DOWN).equals((thirdPerson)));
    }

    @Test
    void pollFirstPersonFromTheLongestQueue() {
        Floor floor = building.getFloorWithIndex(1);
        Floor upperFloor = building.getFloorWithIndex(2);
        Floor lowerFloor = building.getFloorWithIndex(0);

        Person firstPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person thirdPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);
        floor.addPerson(thirdPerson);

        assertTrue(floor.pollFirstPerson(DirectionOfTravel.NONE).equals(firstPerson));
    }

    @Test
    void pollFirstPersonFromTheUpQueue() {
        Floor floor = building.getFloorWithIndex(1);
        Floor upperFloor = building.getFloorWithIndex(2);
        Floor lowerFloor = building.getFloorWithIndex(0);

        Person firstPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person thirdPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);
        floor.addPerson(thirdPerson);

        assertTrue(floor.pollFirstPerson(DirectionOfTravel.UP).equals(firstPerson));
    }

    @Test
    void poolFirstPersonFromTheDownQueue() {
        Floor floor = building.getFloorWithIndex(1);
        Floor upperFloor = building.getFloorWithIndex(2);
        Floor lowerFloor = building.getFloorWithIndex(0);

        Person firstPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person thirdPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);
        floor.addPerson(thirdPerson);

        assertTrue(floor.pollFirstPerson(DirectionOfTravel.DOWN).equals(secondPerson));
    }

    @Test
    void getNumberOfPeople() {
        Floor floor = building.getFloorWithIndex(1);
        Floor firstUpperFloor = building.getFloorWithIndex(2);
        Floor secondUpperFloor = building.getFloorWithIndex(3);

        Person firstPerson= Person.of(50, firstUpperFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, secondUpperFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);

        DirectionOfTravel direction = firstPerson.getTrip().getDirection();

        assertTrue(floor.getNumberOfPeople(direction) == 2);
    }

    @Test
    void getNumberOfPeopleFromTheLongestQueue() {
        Floor floor = building.getFloorWithIndex(1);
        Floor upperFloor = building.getFloorWithIndex(2);
        Floor lowerFloor = building.getFloorWithIndex(0);

        Person firstPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person thirdPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);
        floor.addPerson(thirdPerson);

        assertTrue(floor.getNumberOfPeople(DirectionOfTravel.NONE) == 2);
    }

    @Test
    void getNumberOfPeopleFromUpQueue() {
        Floor floor = building.getFloorWithIndex(1);
        Floor upperFloor = building.getFloorWithIndex(2);
        Floor lowerFloor = building.getFloorWithIndex(0);

        Person firstPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());
        Person thirdPerson = Person.of(50, lowerFloor, upperFloor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);
        floor.addPerson(thirdPerson);

        assertTrue(floor.getNumberOfPeople(DirectionOfTravel.UP) == 2);
    }

    @Test
    void getNumberOfPeopleFromDownQueue() {
        Floor floor = building.getFloorWithIndex(1);
        Floor upperFloor = building.getFloorWithIndex(2);
        Floor lowerFloor = building.getFloorWithIndex(0);

        Person firstPerson = Person.of(50, upperFloor, floor.getNumberOfFloor());
        Person secondPerson = Person.of(50, lowerFloor, floor.getNumberOfFloor());
        Person thirdPerson = Person.of(50, lowerFloor, upperFloor.getNumberOfFloor());

        floor.addPerson(firstPerson);
        floor.addPerson(secondPerson);
        floor.addPerson(thirdPerson);

        assertTrue(floor.getNumberOfPeople(DirectionOfTravel.DOWN) == 1);
    }

    @Test
    void callElevatorTest() {
        Floor floor = building.getFloorWithIndex(1);
        DirectionOfTravel direction = DirectionOfTravel.DOWN;

        floor.callElevator(direction);

        assertTrue(building.getController().getAllTrips().contains(Trip.of(floor.getNumberOfFloor(), direction)));
    }
}
