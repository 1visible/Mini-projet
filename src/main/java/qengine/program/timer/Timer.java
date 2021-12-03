package qengine.program.timer;

import java.util.HashMap;
import java.util.Map;

public class Timer {
    private static Map<Type, Timer> timers = new HashMap<Type, Timer>();

    public void start(Type timer) {

    }

    public void end(Type timer) {

    }

    public static Timer get(Type timer) {
        return timers.get(timer);
    }

}
