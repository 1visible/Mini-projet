package qengine.program.index;

public enum Type {
    OPS(2, 1, 0),
    OSP(2, 0, 1),
    POS(1, 2, 0),
    PSO(1, 0, 2),
    SOP(0, 2, 1),
    SPO(0, 1, 2);

    public final int first;
    public final int second;
    public final int third;

    Type(int first, int second, int third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
