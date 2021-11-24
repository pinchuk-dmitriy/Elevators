package pinchuk.dmitriy.issoft.domain.buildings;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


@Getter
@EqualsAndHashCode
public class Trip {

    private final int targetFloorWithIndexNumber;
    private final DirectionOfTravel direction;

    private Trip(int targetFloorWithIndexNumber, DirectionOfTravel direction) {
        checkArgument(targetFloorWithIndexNumber >= Floor.NUMBER_OF_FIRST_FLOOR);
        checkNotNull(direction);

        this.targetFloorWithIndexNumber = targetFloorWithIndexNumber;
        this.direction = direction;
    }

    public static Trip of(int targetFloorWithIndexNumber, DirectionOfTravel direction) {
        return new Trip(targetFloorWithIndexNumber, direction);
    }

    public static Trip of(Floor targetFloorWithIndex, DirectionOfTravel direction) {
        checkNotNull(targetFloorWithIndex);

        return new Trip(targetFloorWithIndex.getNumberOfFloor(), direction);
    }

    public static Trip of(int targetFloorWithIndexNumber, int startFloorNumber) {
        checkArgument(targetFloorWithIndexNumber != startFloorNumber);

        DirectionOfTravel direction = resolveDirection(targetFloorWithIndexNumber, startFloorNumber);

        return new Trip(targetFloorWithIndexNumber, direction);
    }

    public static Trip of(Floor targetFloorWithIndex, Floor startFloor) {
        checkArgument(targetFloorWithIndex.getNumberOfFloor() != startFloor.getNumberOfFloor());

        DirectionOfTravel direction = resolveDirection(targetFloorWithIndex.getNumberOfFloor(), startFloor.getNumberOfFloor());

        return new Trip(targetFloorWithIndex.getNumberOfFloor(), direction);
    }

    public static Trip of(int targetFloorWithIndexNumber, Floor startFloor) {
        checkArgument(targetFloorWithIndexNumber != startFloor.getNumberOfFloor());

        DirectionOfTravel direction = resolveDirection(targetFloorWithIndexNumber, startFloor.getNumberOfFloor());

        return new Trip(targetFloorWithIndexNumber, direction);
    }

    public static Trip of(Floor targetFloorWithIndex, int startFloorNumber) {
        checkArgument(targetFloorWithIndex.getNumberOfFloor() != startFloorNumber);

        DirectionOfTravel direction = resolveDirection(targetFloorWithIndex.getNumberOfFloor(), startFloorNumber);

        return new Trip(targetFloorWithIndex.getNumberOfFloor(), direction);
    }

    private static DirectionOfTravel resolveDirection(int targetFloorWithIndexNumber, int startFloorNumber) {
        return targetFloorWithIndexNumber > startFloorNumber ? DirectionOfTravel.UP : DirectionOfTravel.DOWN;
    }

    @Override
    public String toString() {
        return String.format("(To->%S; Direction:%s)", targetFloorWithIndexNumber, direction);
    }
}