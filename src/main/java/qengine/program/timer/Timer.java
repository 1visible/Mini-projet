package qengine.program.timer;

import java.util.HashMap;
import java.util.Map;

public class Timer {
    private static final Map<Watch, Timer> timers = new HashMap<>();
    private boolean started;
    private long startTime;
    private long totalTime;

    private Timer(Watch watch) {
        timers.put(watch, this);
        totalTime = 0;
    }

    public static void start(Watch watch) {
        Timer timer = timers.containsKey(watch) ? timers.get(watch) : new Timer(watch);
        timer.start();
    }

    public static void stop(Watch watch) throws NullPointerException {
        timers.get(watch).stop();
    }

    public static long get(Watch watch) throws NullPointerException {
        return timers.get(watch).totalTime;
    }

    private void start() throws UnsupportedOperationException {
        if(started)
            throw new UnsupportedOperationException("Le timer est déjà lançé !");

        started = true;
        startTime = System.currentTimeMillis();
    }

    private void stop() throws UnsupportedOperationException {
        totalTime += (System.currentTimeMillis() - startTime);

        if(!started)
            throw new UnsupportedOperationException("Le timer n'est pas lançé !");

        started = false;
    }
}
