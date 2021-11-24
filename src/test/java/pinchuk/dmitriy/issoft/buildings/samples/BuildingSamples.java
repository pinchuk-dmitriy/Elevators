package pinchuk.dmitriy.issoft.buildings.samples;

import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.buildings.Controller;

public class BuildingSamples {

    public static Building anyValidBuilding() {
        Building building = Building.of(1, 10, 2, "address");
        building.setController(new Controller());

        return building;
    }

    public static Building anyInvalidBuilding() {
        Building building = Building.of(1, 0, 0, null);

        return building;
    }

    public static Building anyBuildingWithoutController() {
        Building building = Building.of(1, 10, 3, "address");

        return building;
    }

}
