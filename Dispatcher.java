import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Dispatcher implements Runnable {
    private final List<Elevator> elevators;
    private final BlockingQueue<Request> requestQueue;
    private final Logger logger;

    public Dispatcher(List<Elevator> elevators) {
        this.elevators = elevators;
        this.requestQueue = new LinkedBlockingQueue<>();
        this.logger = Logger.getLogger("Dispatcher");
        logger.setUseParentHandlers(true);
    }

    public void submitRequest(Request request) {
        requestQueue.offer(request);
        logger.info("Person called an elevator " + request);
    }
    @Override
    public void run() {
        while (true) {
            try {
                Request request = requestQueue.take();
                assignElevator(request);
            } catch (InterruptedException e) {
                logger.warning("Dispatcher interrupted: " + e.getMessage());
                break;
            }
        }
    }

    private void assignElevator(Request request) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            int distance = Math.abs(elevator.getCurrentFloor() - request.sourceFloor);
            boolean compatibleDirection =
                    (elevator.getState() == ElevatorState.IDLE) ||
                            (request.goingUp == elevator.directionUp);

            if (compatibleDirection && distance < minDistance) {
                minDistance = distance;
                bestElevator = elevator;
            }
        }

        if (bestElevator != null) {
            bestElevator.addDestination(request.sourceFloor);
            bestElevator.addDestination(request.targetFloor);
            logger.info(String.format("Elevator-%d assigned", bestElevator.getId()));
        } else {
            for (Elevator elevator : elevators) {
                if (elevator.getState() == ElevatorState.IDLE) {
                    elevator.addDestination(request.sourceFloor);
                    elevator.addDestination(request.targetFloor);
                    logger.info(String.format("Backup assignment: Request %s on Elevator-%d", request, elevator.getId()));
                    return;
                }
            }
            logger.warning("There is no available elevator for request " + request);
        }
    }
}
