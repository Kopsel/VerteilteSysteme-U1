package task2b;

/**
 * Contains the basic mechanisms of the Lamport clock
 */
public class LamportClock {
    private int time;

    public LamportClock() {
        this.time = 0;
    }

    public int getTime() {
        return time;
    }

    public void tick() {
        time++;
    }

    public void update(int receivedTime) {
        time = Math.max(time, receivedTime) + 1;
    }

    public int sendEvent() {
        tick();
        return getTime();
    }

    public void receiveEvent(int receivedTime) {
        update(receivedTime);
    }
}
