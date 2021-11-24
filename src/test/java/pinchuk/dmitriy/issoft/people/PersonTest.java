package pinchuk.dmitriy.issoft.people;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.buildings.DirectionOfTravel;
import pinchuk.dmitriy.issoft.domain.buildings.Floor;
import pinchuk.dmitriy.issoft.domain.buildings.Trip;
import pinchuk.dmitriy.issoft.domain.people.Person;
import pinchuk.dmitriy.issoft.people.samples.PersonSamples;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.*;

public class PersonTest {

    public static int MIN_WEIGHT = 10;
    public static Building building;

    @BeforeEach
    void init() {
        building = BuildingSamples.anyValidBuilding();
    }

    @Test
    void createValidPerson() {
        Person person = PersonSamples.anyValidPerson();

        checkNotNull(person.getCurrentFloor());
        assertTrue(person.getWeight() >= MIN_WEIGHT);
        assertTrue(person.getNeededFloor() < building.getNumberOfFloors() && person.getNeededFloor() >= Floor.NUMBER_OF_FIRST_FLOOR);
    }

    @Test
    void createInvalidPerson() {
        assertThrows(IllegalArgumentException.class, () -> PersonSamples.anyInvalidPerson());
    }

    @Test
    void createInvalidPersonWithTheSameFloor() {
        Floor startFloor = building.getFloorWithIndex(3);

        assertThrows(IllegalArgumentException.class, () -> Person.of(50, startFloor, 3));
    }

    @Test
    void getWeightTest() {
        Person person = Person.of(50, building.getFloorWithIndex(0), 5);

        assertTrue(person.getWeight() == 50);
    }

    @Test
    void getTargetFloorWithIndexNumberTest() {
        Person person = Person.of(50, building.getFloorWithIndex(3), 5);

        assertTrue(person.getTrip().getTargetFloorWithIndexNumber() == 5);
    }

    @Test
    void getTripTest() {
        Person person = PersonSamples.anyValidPerson();

        assertTrue(person.getTrip().equals(Trip.of(5, DirectionOfTravel.UP)));
    }

    @Test
    void pushUpButtonTest() {
        Person person = PersonSamples.anyValidPersonWithUpDirection(building);

        person.pushButton();

        assertTrue(building.getController().getAllTrips().contains(Trip.of(person.getCurrentFloor(), person.getTrip().getDirection())));
    }

    @Test
    void pushDownButtonTest() {
        Person person = PersonSamples.anyValidPersonWithDownDirection(building);

        person.pushButton();

        assertTrue(building.getController().getAllTrips().contains(Trip.of(person.getCurrentFloor(), person.getTrip().getDirection())));
    }

}
