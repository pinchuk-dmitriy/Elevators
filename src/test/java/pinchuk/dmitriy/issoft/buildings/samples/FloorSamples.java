package pinchuk.dmitriy.issoft.buildings.samples;

import pinchuk.dmitriy.issoft.domain.buildings.Floor;

public class FloorSamples {

    public static Floor anyValidFloor() {
        return Floor.of(1, BuildingSamples.anyValidBuilding());
    }

    public static Floor anyInvalidFloor() {
        return Floor.of(5000, null);
    }
}
