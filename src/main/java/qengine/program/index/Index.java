package qengine.program.index;

import java.util.*;

public class Index {
    private final Map<Integer, Map<Integer, List<Integer>>> values;
    private final Type type;
    private static final Map<Type, Index> instances = new HashMap<>();

    private Index(Type type) {
        values = new TreeMap<>();
        this.type = type;
    }

    public static Index getInstance(Type type) {
        if(!instances.containsKey(type))
            instances.put(type, new Index(type));

        return instances.get(type);
    }

    public void add(int first, int second, int third) {
        int[] val = order(first, second, third);

        if(values.containsKey(val[0])) {
            Map<Integer, List<Integer>> sValues = values.get(val[0]);

            if(sValues.containsKey(val[1])) {
                sValues.get(val[1]).add(val[2]);
            } else {
                List<Integer> tValues = new ArrayList<>();
                tValues.add(val[2]);
                sValues.put(val[1], tValues);
            }
        } else {
            Map<Integer, List<Integer>> sValues = new TreeMap<>();
            List<Integer> tValues = new ArrayList<>();
            tValues.add(val[2]);
            sValues.put(val[1], tValues);
            values.put(val[0], sValues);
        }
    }

    private int[] order(int first, int second, int third) {
        int[] val = new int[3];
        val[type.first] = first;
        val[type.second] = second;
        val[type.third] = third;

        return val;
    }
}
