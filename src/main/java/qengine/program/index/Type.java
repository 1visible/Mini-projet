package qengine.program.index;

public enum Type {
    OPS(2, 1, 0),
    OSP(1, 2, 0),
    POS(2, 0, 1),
    PSO(1, 0, 2),
    SOP(0, 2, 1),
    SPO(0, 1, 2);

    public final int S;
    public final int P;
    public final int O;

    Type(int S, int P, int O) {
        this.S = S;
        this.P = P;
        this.O = O;
    }
}
