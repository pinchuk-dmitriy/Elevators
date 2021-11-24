package pinchuk.dmitriy.issoft.buildings;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.buildings.Controller;
import pinchuk.dmitriy.issoft.domain.buildings.DirectionOfTravel;

import java.util.concurrent.TimeUnit;
import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BuildingTest {

    public static Building building;

    @BeforeEach
    void init() {
        building = BuildingSamples.anyValidBuilding();
    }
    @Test
    void createValidBuilding() {
        Building building = BuildingSamples.anyValidBuilding();

        assertNotNull(building.getController());
        assertNotNull(building.getElevators());
        assertNotNull(building.getFloors());
        assertNotNull(building.getNumberOfFloors() > 0);
    }

    @Test
    void createInvalidBuilding() {
        assertThrows(IllegalArgumentException.class, () -> BuildingSamples.anyInvalidBuilding());
    }

    @Test
    void getValidFloor() {
        int validFloorId = building.getNumberOfFloors() - 1;

        assertTrue(building.getFloorWithIndex(validFloorId).getNumberOfFloor() == validFloorId);
    }

    @Test
    void getInvalidFloor() {
        int invalidFloorId = building.getNumberOfFloors() + 1;

        assertThrows(IllegalArgumentException.class, () -> building.getFloorWithIndex(invalidFloorId));
    }

    @Test
    void getNumberOfFloors() {
        assertTrue(building.getNumberOfFloors() == 10);
    }

    @Test
    void getFloorWithIndex() {
        assertFalse(building.getFloors().isEmpty());
    }

    @Test
    void addController() {
        Controller controller = new Controller();
        building.setController(controller);

        assertTrue(building.getController().equals(controller));
    }

    @Test
    void addNullController() {
        assertThrows(NullPointerException.class, () -> building.setController(null));
    }

    @Test
    void startElevatorsAndControllerAndPeopleSpawn() {
        assertFalse(building.getElevators().isEmpty());
        assertNotNull(building.getController());
        assertDoesNotThrow(() -> building.startAllElevators());
        assertDoesNotThrow(() -> building.startController());
        assertDoesNotThrow(building::stop);
    }

    @Test
    void startWithoutController() {
        building = BuildingSamples.anyBuildingWithoutController();

        assertThrows(NullPointerException.class, building::startController);
    }

    @Test
    void stopNullController() {
        building = BuildingSamples.anyBuildingWithoutController();

        assertThrows(NullPointerException.class, building::stopController);
    }

    @SneakyThrows
    @Test
    void deliverPeople() {
        building.startAllElevators();
        building.startAllPeopleSpawn();
        building.startController();

        int numberOfPeople = building.getFloors().stream().mapToInt(i -> i.getNumberOfPeople(DirectionOfTravel.UP)
                + i.getNumberOfPeople(DirectionOfTravel.DOWN)).sum();
        TimeUnit.SECONDS.sleep(40);

        assertTrue(numberOfPeople == 0);
    }
}
