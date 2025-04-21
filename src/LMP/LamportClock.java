package LMP;

public class LamportClock {
    private long time;

    public LamportClock() {
        this.time = 0;
    }

    public synchronized long increment() {
        return ++time;
    }

    public synchronized void update(long otherTime) {
        time = Math.max(time, otherTime) + 1;
    }

    public synchronized long getTime() {
        return time;
    }
}
