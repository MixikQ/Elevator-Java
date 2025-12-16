import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import java.util.logging.Formatter;

public class Building {
    private static final int NUM_FLOORS = 10;
    private static final int NUM_ELEVATORS = 3;

    public static void main(String[] args) throws Exception {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler h : handlers) {
            rootLogger.removeHandler(h);
        }

        ConsoleHandler myHandler = new ConsoleHandler();
        myHandler.setFormatter(new Formatter() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
            @Override
            public String format(LogRecord record) {
                return String.format("[%1$s] %2$s: %3$s%n",
                        dateFormat.format(new Date(record.getMillis())),
                        record.getLoggerName(),
                        record.getMessage());
            }
        });
        rootLogger.addHandler(myHandler);
        rootLogger.setLevel(Level.INFO);

        List<Elevator> elevators = new ArrayList<>();
        for (int i = 1; i <= NUM_ELEVATORS; i++) {
            Elevator elevator = new Elevator(i, 1);
            elevators.add(elevator);
            Thread elevatorThread = new Thread(elevator, "Elevator-" + i);
            elevatorThread.setDaemon(false);
            elevatorThread.start();
        }

        Dispatcher dispatcher = new Dispatcher(elevators);
        Thread dispatcherThread = new Thread(dispatcher, "Dispatcher");
        dispatcherThread.setDaemon(false);
        dispatcherThread.start();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleAtFixedRate(() -> {
            int from = (int) (Math.random() * NUM_FLOORS) + 1;
            int to = (int) (Math.random() * NUM_FLOORS) + 1;

            if (from != to) {
                Request request = new Request(from, to);
                dispatcher.submitRequest(request);
            }
        }, 0, 10, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            System.out.println("[Simulation] Process ends in 10 seconds...");

            try { Thread.sleep(10000); }
            catch (InterruptedException e) { /* ignore */ }

            scheduler.shutdown();

            dispatcherThread.interrupt();
            for (Elevator elevator : elevators) Thread.currentThread().interrupt();
            System.out.println("[Simulation] Process ended.");
            System.exit(0);

        }, 60, TimeUnit.SECONDS);
    }
}
