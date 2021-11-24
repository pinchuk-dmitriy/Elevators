package pinchuk.dmitriy.issoft.domain.people;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pinchuk.dmitriy.issoft.domain.buildings.DirectionOfTravel;
import pinchuk.dmitriy.issoft.domain.buildings.Floor;
import pinchuk.dmitriy.issoft.domain.buildings.Trip;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Setter
@EqualsAndHashCode
public class Person {

    public static int MIN_WEIGHT = 10;
    public static int MAX_WEIGHT = 180;

    private final String id;
    private final int weight;
    private final Floor currentFloor;
    private final int neededFloor;
    private final Trip trip;

    public static Person of(int weight, Floor currentFloor, int neededFloor) {
        return new Person(weight, currentFloor, neededFloor);
    }

    private Person(int weight, Floor currentFloor, int neededFloor) {
        checkNotNull(currentFloor);
        checkArgument(neededFloor >= Floor.NUMBER_OF_FIRST_FLOOR);
        checkArgument(neededFloor != currentFloor.getNumberOfFloor());
        checkArgument(weight >= MIN_WEIGHT && weight <= MAX_WEIGHT);

        this.id = UUID.randomUUID().toString();
        this.weight = weight;
        this.currentFloor = currentFloor;
        this.neededFloor = neededFloor;
        this.trip = Trip.of(neededFloor,
                neededFloor - currentFloor.getNumberOfFloor() > 0 ? DirectionOfTravel.UP : DirectionOfTravel.DOWN);

    }

    public void pushButton() {
        currentFloor.callElevator(trip.getDirection());
    }

}
