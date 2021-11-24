package pinchuk.dmitriy.issoft.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.buildings.samples.BuildingSamples;
import pinchuk.dmitriy.issoft.domain.buildings.Building;
import pinchuk.dmitriy.issoft.domain.util.BuildPrintingUtil;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BuildPrintingUtilTest {

    public static Building building;
    @BeforeEach
    void init() {
        building = BuildingSamples.anyValidBuilding();
    }

    @Test
    void createValidBuildPrintingUtil() {
        assertDoesNotThrow(() -> BuildPrintingUtil.of(building, 1000));
    }

    @Test
    void createInvalidFastBuildPrintingUtil() {
        assertThrows(IllegalArgumentException.class,
                () -> BuildPrintingUtil.of(building, 1001));
    }

    @Test
    void createInvalidSlowBuildPrintingUtil() {
        assertThrows(IllegalArgumentException.class,
                () -> BuildPrintingUtil.of(building, 99));
    }

    @Test
    void createInvalidBuildPrintingUtilWithNullBuildingTest() {
        assertThrows(NullPointerException.class,
                () -> BuildPrintingUtil.of(null, 1000));
    }

    @Test
    void turnOff() {
        BuildPrintingUtil buildPrintingUtil = BuildPrintingUtil.of(building, 1000);

        buildPrintingUtil.turnOn();
        buildPrintingUtil.turnOff();

        assertFalse(buildPrintingUtil.isRunning());
    }

    @Test
    void turnOn() {
        BuildPrintingUtil buildPrintingUtil = BuildPrintingUtil.of(building, 1000);

        buildPrintingUtil.turnOn();

        assertTrue(buildPrintingUtil.isRunning());
    }
}
