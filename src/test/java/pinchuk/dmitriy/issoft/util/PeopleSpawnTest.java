package pinchuk.dmitriy.issoft.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.buildings.DirectionOfTravel;
import pinchuk.dmitriy.issoft.domain.util.PeopleSpawn;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PeopleSpawnTest {

    public static Building building;

    @BeforeEach
    void init() {
        building = BuildingSamples.anyValidBuilding();
    }

    @Test
    void createValidPeopleSpawn() {
        assertDoesNotThrow(() -> PeopleSpawn.of(building.getFloorWithIndex(0)));
    }

    @Test
    void createInvalidPeopleSpawnWithoutFloor() {
        assertThrows(NullPointerException.class, () -> PeopleSpawn.of(null));
    }

    @Test
    void generatePeople() {
        building.getFloorWithIndex(0).getPeopleSpawn().generatePeople();
        building.getFloorWithIndex(1).getPeopleSpawn().generatePeople();
        building.getFloorWithIndex(2).getPeopleSpawn().generatePeople();
        int actualNumberOfPeople = building.getFloors().stream()
                .mapToInt(i -> i.getNumberOfPeople(DirectionOfTravel.UP) + i.getNumberOfPeople(DirectionOfTravel.DOWN)).sum();

        assertTrue(actualNumberOfPeople != 0);
    }

    @Test
    void unpause() {
        building.getFloorWithIndex(0).getPeopleSpawn().unpause();

        assertTrue(building.getFloorWithIndex(0).getPeopleSpawn().isWorking());
    }

    @Test
    void pause() {

        building.getFloorWithIndex(0).getPeopleSpawn().unpause();
        building.getFloorWithIndex(0).getPeopleSpawn().pause();

        assertFalse(building.getFloorWithIndex(0).getPeopleSpawn().isWorking());
    }

}
