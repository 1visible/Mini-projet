package qengine.program.dictionary;

import java.util.HashMap;
import java.util.Map;

public class Dictionary {
    private final Map<String, Integer> valToInd;
    private final Map<Integer, String> indToVal;
    private int counter;
    private static Dictionary instance;

    private Dictionary() {
        valToInd = new HashMap<>();
        indToVal = new HashMap<>();
        counter = 1;
    }

    public static Dictionary getInstance() {
        if(instance == null)
            instance = new Dictionary();

        return instance;
    }

    public String get(int index) throws NullPointerException {
        return indToVal.get(index);
    }

    public int get(String value) throws NullPointerException {
        return valToInd.get(value);
    }

    public void add(String... values) {
        for(String value : values) {
            if(!valToInd.containsKey(value)) {
                valToInd.put(value, counter);
                indToVal.put(counter, value);
                counter++;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(Map.Entry<String, Integer> entry : valToInd.entrySet()) {
            builder.append("<")
                   .append(entry.getKey())
                   .append(", ")
                   .append(entry.getValue())
                   .append(">\n");
        }

        return builder.toString();
    }
}
