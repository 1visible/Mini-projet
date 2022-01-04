package qengine.program.timer;

/**
 * Enumeration des champs qui nécessite un tracking de temps
 */
public enum Watch {
    DATA_READ,
    QUERIES_READ,
    DICT_CREATION,
    IND_CREATION,
    WORKLOAD,
    TOTAL,
}
