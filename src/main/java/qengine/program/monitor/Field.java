package qengine.program.monitor;

/**
 * Enumeration des champs qui vont être traqués durant l'exécution
 */
public enum Field {
    DATA_FILENAME("data_filename", "Nom du fichier de données : %s"),
    QUERIES_FILENAME("queries_filename", "Nom du fichier de requêtes : %s"),
    RDF_TRIPLES_COUNT("rdf_triples_count", "Nombre de triplets : %s"),
    QUERIES_COUNT("queries_count", "Nombre de requêtes : %s"),
    QUERIES_WITHOUT_RESULT_COUNT("queries_whitout_result_count", "Nombre de requêtes sans résultat: %s"),
    DATA_READ_TIME("data_read_time", "Temps de lecture des données (ms) : %s"),
    QUERIES_READ_TIME("queries_read_time", "Temps de lecture des requêtes (ms) : %s"),
    DICT_CREATION_TIME("dictionary_creation_time", "Temps de création du dictionnaire (ms) : %s"),
    INDEX_COUNT("index_count", "Nombre d'index : %s"),
    IND_CREATION_TIME("index_creation_time", "Temps de création des index (ms) : %s"),
    WORKLOAD_TIME("workload_time", "Temps total d'évaluation du workload (ms) : %s"),
    TOTAL_TIME("total_time", "Temps total (du début à la fin du programme) : %s");

    private final String name;
    private final String formattedName;
    private String strValue;
    private long numValue;

    Field(String name, String formattedName) {
        this.name = name;
        this.formattedName = formattedName;
        this.strValue = "";
        this.numValue = 0;
    }

    public void append(String value) {
        strValue += value;
    }

    public void append(long value) {
        numValue += value;
    }

    public void append(int value) {
        numValue += value;
    }

    public String get() {
        return strValue.isEmpty() ? String.valueOf(numValue) : strValue;
    }

    public long getNumValue() {
        return numValue;
    }

    public String formatted() {
        return formattedName;
    }

    @Override
    public String toString() {
        return name;
    }
}
