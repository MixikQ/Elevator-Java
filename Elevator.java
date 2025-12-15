import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class Elevator implements Runnable {
    private final int id;
    private int currentFloor;
    private ElevatorState state;
    boolean directionUp;
    private final BlockingQueue<Integer> destinations;
    private final Logger logger;
    private final ReentrantLock lock;

    public Elevator(int id, int startFloor) {
        this.id = id;
        this.currentFloor = startFloor;
        this.state = ElevatorState.IDLE;
        this.directionUp = true;
        this.destinations = new LinkedBlockingQueue<>();
        this.logger = Logger.getLogger("Elevator-" + id);
        logger.setUseParentHandlers(true);
        this.lock = new ReentrantLock();
    }

    public int getId() { return id; }
    public int getCurrentFloor() { return currentFloor; }
    public ElevatorState getState() { return state; }

    public void addDestination(int floor) {
        destinations.offer(floor);
    }

    @Override
    public void run() {
        while (true) {
            try {
                lock.lock();

                if (state == ElevatorState.IDLE && destinations.isEmpty()) {
                    lock.unlock();
                    Thread.sleep(2000);
                    continue;
                }

                if (!destinations.isEmpty()) {
                    int nextFloor = destinations.peek();
                    directionUp = nextFloor > currentFloor;
                    logger.info(String.format("Elevator-%d: Moving from %d to %d", id, currentFloor, nextFloor));
                    state = ElevatorState.MOVING;
                    while (currentFloor != nextFloor) {
                        if (directionUp) currentFloor++;
                        else currentFloor--;
                        logger.info(String.format("Elevator-%d: Floor %d", id, currentFloor));
                        Thread.sleep(2000);
                    }

                    destinations.poll();
                    arriveAtFloor(currentFloor);
                }

                state = ElevatorState.IDLE;
                lock.unlock();
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                logger.warning("Elevator-" + id + " interrupted: " + e.getMessage());
                break;
            }
        }
    }

    private void arriveAtFloor(int floor) throws InterruptedException {
        state = ElevatorState.DOORS_OPEN;
        logger.info(String.format("Elevator-%d: Arrived at floor %d, opens the doors", id, floor));
        Thread.sleep(1000);
        logger.info(String.format("Elevator-%d: Closes the doors", id));
        Thread.sleep(500);
    }
}
