package pinchuk.dmitriy.issoft.buildings.samples;

import pinchuk.dmitriy.issoft.domain.buildings.DirectionOfTravel;
import pinchuk.dmitriy.issoft.domain.buildings.Trip;

public class TripSamples {

    public static Trip anyValidTrip() {
        return Trip.of(5, DirectionOfTravel.UP);
    }

    public static Trip anyInvalidTrip() {
        return Trip.of(5000, DirectionOfTravel.NONE);
    }
}
