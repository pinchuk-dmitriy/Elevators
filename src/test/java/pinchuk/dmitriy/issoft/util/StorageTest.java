package pinchuk.dmitriy.issoft.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pinchuk.dmitriy.issoft.domain.util.Storage;

import static junit.framework.Assert.assertTrue;

public class StorageTest {

    @BeforeEach
    void init() {
        Storage.getInstance().restart();
    }

    @Test
    void restart() {
        Storage storage = Storage.getInstance();

        storage.incrementNumberOfDeliveredPeople();
        storage.incrementNumberOfGeneratedPeople();
        storage.incrementNumberOfPassedFloors();

        storage.restart();

        assertTrue(storage.getNumberOfDeliveredPeople() == 0);
        assertTrue(storage.getNumberOfGeneratedPeople() == 0);
        assertTrue(storage.getNumberOfPassedFloors() == 0);
    }

    @Test
    void incrementNumberOfDeliveredPeople() {
        Storage storage = Storage.getInstance();

        storage.incrementNumberOfDeliveredPeople();
        storage.incrementNumberOfDeliveredPeople();
        storage.incrementNumberOfDeliveredPeople();

        assertTrue(storage.getNumberOfDeliveredPeople() == 3);
    }

    @Test
    void incrementNumberOfGeneratedPeople() {
        Storage storage = Storage.getInstance();

        storage.incrementNumberOfGeneratedPeople();
        storage.incrementNumberOfGeneratedPeople();
        storage.incrementNumberOfGeneratedPeople();

        assertTrue(storage.getNumberOfGeneratedPeople() == 3);
    }

    @Test
    void incrementNumberOfPassedFloors() {
        Storage storage = Storage.getInstance();

        storage.incrementNumberOfPassedFloors();
        storage.incrementNumberOfPassedFloors();
        storage.incrementNumberOfPassedFloors();

        assertTrue(storage.getNumberOfPassedFloors() == 3);
    }

    @Test
    void getNumberOfDeliveredPeople() {
        Storage storage = Storage.getInstance();

        assertTrue(storage.getNumberOfDeliveredPeople() == 0);
    }

    @Test
    void getNumberOfGeneratedPeople() {
        Storage storage = Storage.getInstance();

        assertTrue(storage.getNumberOfGeneratedPeople() == 0);
    }

    @Test
    void getNumberOfPassedFloors() {
        Storage storage = Storage.getInstance();

        assertTrue(storage.getNumberOfPassedFloors() == 0);
    }
}
