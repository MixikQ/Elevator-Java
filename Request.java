public class Request {
    final int sourceFloor;
    final int targetFloor;
    final boolean goingUp;
    final long timestamp;

    public Request(int source, int target) {
        this.sourceFloor = source;
        this.targetFloor = target;
        this.goingUp = target > source;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("from %d to %d ", sourceFloor, targetFloor);
    }
}
