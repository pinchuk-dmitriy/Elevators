package pinchuk.dmitriy.issoft.buildings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.domain.buildings.*;
import pinchuk.dmitriy.issoft.domain.people.Person;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ControllerTest {

    public static Building building;

    @BeforeEach
    void init() {
        building = BuildingSamples.anyValidBuilding();
        building.getElevators().forEach(i -> i.setLiftingCapacity(200));
    }

    @Test
    void createValidController() {
        Controller controller = Controller.of(building.getElevators());

        assertNotNull(controller.getElevators());
        assertNotNull(controller.getTrips());
    }

    @Test
    void createInvalidController() {
        assertThrows(NullPointerException.class, () -> Controller.of(null));
    }

    @Test
    void getAllTrips() {

        building.getElevators().forEach(i -> i.setLiftingCapacity(200));
        Person firstPerson = Person.of(50, building.getFloorWithIndex(3),5 );
        Person secondPerson = Person.of(50, building.getFloorWithIndex(3), 8);

        firstPerson.getCurrentFloor().addPerson(firstPerson);
        secondPerson.getCurrentFloor().addPerson(secondPerson);

        building.getController().addTrip(firstPerson.getTrip());
        building.getController().addTrip(secondPerson.getTrip());

        assertTrue(building.getController().getAllTrips().contains(Trip.of(firstPerson.getCurrentFloor(), firstPerson.getTrip().getDirection()))
                || building.getController().getAllTrips().contains(Trip.of(secondPerson.getCurrentFloor(), secondPerson.getTrip().getDirection())));
    }

    @Test
    void addTrip() {
        Person firstPerson = Person.of(50, building.getFloorWithIndex(3), 5);
        building.getController().addTrip(firstPerson.getTrip());

        assertTrue(building.getController().getAllTrips().contains(firstPerson.getTrip()));
    }

    @Test
    void removeTrip() {
        Person firstPerson = Person.of(50, building.getFloorWithIndex(3), 5);

        building.getController().addTrip(firstPerson.getTrip());
        building.getController().removeTrip(firstPerson.getTrip());

        assertFalse(building.getController().getAllTrips().contains(firstPerson.getTrip()));
    }

    @Test
    void sendTrip() {
        Person firstPerson = Person.of(50, building.getFloorWithIndex(3), 5);

        firstPerson.getCurrentFloor().addPerson(firstPerson);

        building.getController().sendTrip();

        assertFalse(building.getController().getAllTrips().contains(firstPerson.getTrip()));
        assertNotNull(building.getElevators().stream()
                .filter(i -> i.getTrips().contains(Trip.of(firstPerson.getCurrentFloor(), DirectionOfTravel.UP)))
                .findFirst());
    }

    @Test
    void sendTripToIdleElevator() {
        building.getElevators().forEach(i -> i.setLiftingCapacity(200));

        Trip trip = Trip.of(8, 3);

        building.getElevators().get(1).goUp();

        building.getController().addTrip(trip);
        building.getController().sendTrip();

        assertFalse(building.getController().getAllTrips().contains(trip));
        assertTrue(building.getElevators().get(0).getTrips().contains(trip));
        assertFalse(building.getElevators().get(1).getTrips().contains(trip));
    }

    @Test
    void sendTripToTheNearestElevator() {
        AtomicInteger firstFloor = new AtomicInteger(0);
        AtomicInteger secondFloor = new AtomicInteger(1);

        building.getElevators().get(0).setCurrentFloorNumber(firstFloor);
        building.getElevators().get(1).setCurrentFloorNumber(secondFloor);

        Trip trip = Trip.of(8, 3);

        building.getController().addTrip(trip);
        building.getController().sendTrip();

        assertFalse(building.getController().getAllTrips().contains(trip));
        assertTrue(building.getElevators().get(1).getTrips().contains(trip));
        assertFalse(building.getElevators().get(0).getTrips().contains(trip));
    }

    @Test
    void doNotSendTripToNotSuitableElevator() {
        AtomicInteger firstFloor = new AtomicInteger(5);
        AtomicInteger secondFloor = new AtomicInteger(1);

        building.getElevators().get(0).setCurrentFloorNumber(firstFloor);
        building.getElevators().get(1).setCurrentFloorNumber(secondFloor);

        building.getElevators().get(0).goUp();
        building.getElevators().get(1).goUp();

        Trip trip = Trip.of(8, DirectionOfTravel.DOWN);

        building.getController().addTrip(trip);
        building.getController().sendTrip();

        assertTrue(building.getController().getAllTrips().contains(trip));
        assertFalse(building.getElevators().get(0).getTrips().contains(trip));
        assertFalse(building.getElevators().get(1).getTrips().contains(trip));
    }

    @Test
    void unpause() {
        building.getController().unpause();

        assertTrue(building.getController().isWorking() == true);
    }

    @Test
    void pause() {
        building.getController().unpause();
        building.getController().pause();

        assertTrue(building.getController().isWorking() == false);
    }
}
