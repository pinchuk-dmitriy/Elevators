package pinchuk.dmitriy.issoft;

import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.buildings.Controller;
import pinchuk.dmitriy.issoft.domain.buildings.Elevator;
import pinchuk.dmitriy.issoft.domain.util.BuildPrintingUtil;

public class Runner {
    public static void main(String[] args) {

        Building building1 = Building.of(1, 10, 5, "address").setController(new Controller());
        for (Elevator elevator: building1.getElevators()) {
            elevator.setLiftingCapacity(400);
            elevator.setDoorSpeed(1);
        }
        building1.startAllElevators();
        building1.startAllPeopleSpawn();
        building1.startController();
        Thread util = BuildPrintingUtil.of(building1, 1000);
        util.start();

    }
}
