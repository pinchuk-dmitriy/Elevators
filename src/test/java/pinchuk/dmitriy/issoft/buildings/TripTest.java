package pinchuk.dmitriy.issoft.buildings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.buildings.samples.TripSamples;
import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.buildings.DirectionOfTravel;
import pinchuk.dmitriy.issoft.domain.buildings.Floor;
import pinchuk.dmitriy.issoft.domain.buildings.Trip;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TripTest {

    public static Building building;

    @BeforeEach
    void init() {
        building = BuildingSamples.anyValidBuilding();
    }

    @Test
    void createValidTrip() {
        Trip trip = TripSamples.anyValidTrip();

        assertTrue(trip.getTargetFloorWithIndexNumber()>=0 && trip.getTargetFloorWithIndexNumber()<building.getNumberOfFloors());
        assertNotNull(trip.getDirection());
    }

    @Test
    void createInvalidTrip() {
        Trip trip = TripSamples.anyInvalidTrip();

        assertFalse(trip.getTargetFloorWithIndexNumber()>=0 && trip.getTargetFloorWithIndexNumber()<building.getNumberOfFloors());
        assertTrue(trip.getDirection() == DirectionOfTravel.NONE || trip.getDirection() == null);
    }

    @Test
    void createTripFromFloors() {
        Floor firstFloor = building.getFloorWithIndex(5);
        Floor secondFloor = building.getFloorWithIndex(6);

        assertDoesNotThrow(() -> Trip.of(firstFloor, secondFloor));
    }

    @Test
    void createTripFromFloorsNumber() {
        assertDoesNotThrow(() -> TripSamples.anyValidTrip());
    }

    @Test
    void createTripFromFloorAndFloorNumber() {
        Floor firstFloor = building.getFloorWithIndex(5);

        assertDoesNotThrow(() -> Trip.of(firstFloor, 6));
    }

    @Test
    void createInvalidTripWithTheSameFloorAndFloorNumber() {
        Floor firstFloor = building.getFloorWithIndex(5);

        assertThrows(IllegalArgumentException.class, () -> Trip.of(firstFloor, 5));
    }

    @Test
    void createTripFromFloorNumberAndFloor() {
        Floor secondFloor = building.getFloorWithIndex(6);

        assertDoesNotThrow(() -> Trip.of(5, secondFloor));
    }

    @Test
    void createInvalidTripWithTheSameFloorNumberAndFloor() {
        Floor secondFloor = building.getFloorWithIndex(6);

        assertThrows(IllegalArgumentException.class, () -> Trip.of(6, secondFloor));
    }
}
