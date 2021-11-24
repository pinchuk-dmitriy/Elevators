package pinchuk.dmitriy.issoft.people.samples;

import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.people.Person;

public class PersonSamples {

    public static Person anyValidPerson() {
        return Person.of(50, BuildingSamples.anyValidBuilding().getFloorWithIndex(1), 5);
    }

    public static Person anyInvalidPerson() {
        return Person.of(0, BuildingSamples.anyValidBuilding().getFloorWithIndex(500), 1000);
    }

    public static Person anyValidPersonWithUpDirection(Building building) {
        return Person.of(50, building.getFloorWithIndex(0), 3);
    }

    public static Person anyValidPersonWithDownDirection(Building building) {
        return Person.of(50, building.getFloorWithIndex(5), 3);
    }

}
