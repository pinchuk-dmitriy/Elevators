package pinchuk.dmitriy.issoft.domain.util;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.buildings.ElevatorState;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkArgument;
import static pinchuk.dmitriy.issoft.domain.buildings.ElevatorState.*;

@Slf4j
public class BuildPrintingUtil extends Thread {

    @Getter
    private boolean isRunning;
    private final Building building;
    private final int renderingSpeed;
    private ElevatorState state;
    private BuildPrintingUtil(Building building, int renderingSpeed){
        checkNotNull(building);
        checkArgument(renderingSpeed <= 1000 && renderingSpeed >= 100);

        this.building = building;
        this.renderingSpeed = renderingSpeed;

        String threadName = "Util";
        this.setName(threadName);
    }

    public static BuildPrintingUtil of(Building building, int renderingSpeed){
        return new BuildPrintingUtil(building, renderingSpeed);
    }

    public void printBuilding() {
        System.out.flush();

        System.out.printf("Delivered: %s\n", Storage.getInstance().getNumberOfDeliveredPeople());
        System.out.printf("Generated: %s\n", Storage.getInstance().getNumberOfGeneratedPeople());
        System.out.printf("Floors passed: %s\n", Storage.getInstance().getNumberOfPassedFloors());


        for (int i = 0; i < building.getElevators().size(); i++) {

            state = switch (building.getElevators().get(i).getElevatorState()) {
                case OPEN_DOOR -> OPEN_DOOR;
                case CLOSE_DOOR -> CLOSE_DOOR;
                case LOAD -> LOAD;
                case STOP -> STOP;
                case MOVE -> MOVE;
                case END -> END;
            };

            System.out.println(building.getElevators().get(i));
        }

    }

    private void waitForOperation(){

        try {
            TimeUnit.MILLISECONDS.sleep(renderingSpeed);
        } catch (InterruptedException exception){
            log.error("user interface cannot wait, cause it was interrupted");
            log.error(exception.getMessage());

            Thread.currentThread().interrupt();
        }

    }

    public void turnOff() {
        isRunning = false;
    }

    public void turnOn() {
        isRunning = true;
    }

    @Override
    public void run(){
        turnOn();
        while (isRunning && !isInterrupted()){
            waitForOperation();
            printBuilding();
        }
    }

}
