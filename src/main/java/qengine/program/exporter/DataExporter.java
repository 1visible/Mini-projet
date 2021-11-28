package qengine.program.exporter;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DataExporter {

    private String dataFile;
    private String queryFile;
    private int rdfTripletCount;
    private int queriesCount;
    private double readDataTime;
    private double readQueriesTime;
    private double dictCreationTime;
    private int indexCount;
    private double indexCreationTime;
    private double workloadTime;
    private double totalExecTime;

    private Date tempDate;

    private static DataExporter instance;

    public DataExporter() {
        this.dataFile = "";
        this.queryFile = "";
        this.rdfTripletCount = 0;
        this.queriesCount = 0;
        this.readDataTime = 0.0;
        this.readQueriesTime = 0.0;
        this.dictCreationTime = 0.0;
        this.indexCount = 0;
        this.indexCreationTime = 0.0;
        this.workloadTime = 0.0;
        this.totalExecTime = 0.0;
        this.tempDate = new Date();
    }

    public static DataExporter getInstance() {
        if(instance == null)
            instance = new DataExporter();

        return instance;
    }

    /* SETTERS */

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public void setQueryFile(String queryFile) {
        this.queryFile = queryFile;
    }

    public void setRdfTripletCount(int rdfTripletCount) {
        this.rdfTripletCount = rdfTripletCount;
    }

    public void incrRdfTripletCount() {
        this.rdfTripletCount++;
    }

    public void setQueriesCount(int queriesCount) {
        this.queriesCount = queriesCount;
    }

    public void setReadDataTime(Date finishTime) {
        this.readDataTime = getDateDiff(this.tempDate, finishTime);
    }

    public void incrReadDataTime(Date startTime, Date finishTime) {
        this.readDataTime += getDateDiff(startTime, finishTime);
    }

    public void setReadQueriesTime(Date finishTime) {
        this.readQueriesTime = getDateDiff(this.tempDate, finishTime);
    }

    public void setDictCreationTime(Date finishTime) {
        this.dictCreationTime = getDateDiff(this.tempDate, finishTime);
    }

    public void setIndexCount() {
        this.indexCount = this.rdfTripletCount * 6;
    }

    public void setIndexCreationTime(Date finishTime) {
        this.indexCreationTime = getDateDiff(this.tempDate, finishTime);
    }

    public void incrIndexCreationTime(Date startTime, Date finishTime) {
        this.indexCreationTime += getDateDiff(startTime, finishTime);
    }

    public void setWorkloadTime(Date finishTime) {
        this.workloadTime = getDateDiff(this.tempDate, finishTime);
    }

    public void setTotalExecTime(Date startTime, Date finishTime) {
        this.totalExecTime = getDateDiff(startTime, finishTime);
    }

    public void setTempDate(Date tempDate) {
        this.tempDate = tempDate;
    }

    public double getDateDiff(Date date1, Date date2) {
        return (double) (date2.getTime() - date1.getTime());
    }

    public void writeToCsv(String path) {

        List<String[]> csvData = new ArrayList<>(Arrays.asList(
                new String[]{"data_filename", "query_filename", "rdf_triplet_count", "query_count", "data_reading_time", "query_reading_time", "dictionary_creation_time", "index_count", "index_creation_time", "workload_process_time", "total_program_time"},
                new String[]{dataFile, queryFile, String.valueOf(rdfTripletCount), String.valueOf(queriesCount), String.valueOf(readDataTime), String.valueOf(readQueriesTime), String.valueOf(dictCreationTime), String.valueOf(indexCount), String.valueOf(indexCreationTime), String.valueOf(workloadTime), String.valueOf(totalExecTime)}
        ));

        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            writer.writeAll(csvData);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture des métadonnées dans le fichier CSV \n"+e.getStackTrace());
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\n============ DataExporter =============\n")
                .append("\nNom du fichier de données : ")
                .append(dataFile)
                .append("\nNom du fichier de requêtes : ")
                .append(queryFile)
                .append("\nNombre de triplets : ")
                .append(rdfTripletCount)
                .append("\nNombre de requêtes : ")
                .append(queriesCount)
                .append("\nTemps de lecture des données (ms) : ")
                .append(readDataTime)
                .append("\nTemps de lecture des requêtes (ms) : ")
                .append(readQueriesTime)
                .append("\nTemps de création du dictionnaire (ms) : ")
                .append(dictCreationTime)
                .append("\nNombre d'index : ")
                .append(indexCount)
                .append("\nTemps de création des index (ms) : ")
                .append(indexCreationTime)
                .append("\nTemps total d'évaluation du workload (ms) : ")
                .append(workloadTime)
                .append("\nTemps total (du début à la fin du programme) : ")
                .append(totalExecTime)
                .append("\n====================================");
        return builder.toString();
    }
}
