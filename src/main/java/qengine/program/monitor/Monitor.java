package qengine.program.monitor;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Monitor {
    public static void append(Field field, String value) {
        field.append(value);
    }

    public static void append(Field field, int value) {
        field.append(value);
    }

    public static void append(Field field, long value) {
        field.append(value);
    }

    public static void writeToCsv(String path) {

        List<String[]> csvData = new ArrayList<>(Arrays.asList(
                Arrays.stream(Field.values()).map(Field::toString).toArray(String[]::new),
                Arrays.stream(Field.values()).map(Field::get).toArray(String[]::new)
        ));

        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            writer.writeAll(csvData);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture des métadonnées dans le fichier CSV \n");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        String format = Arrays.stream(Field.values()).map(Field::formatted).collect(Collectors.joining("\n"));
        Object[] args = Arrays.stream(Field.values()).map(Field::get).toArray(String[]::new);

        return String.format("\n============ DataExporter =============\n" +
                format + "\n====================================",
                args
        );
    }
}
