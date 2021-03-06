package qengine.program;

import com.beust.jcommander.JCommander;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Classe permettant de filtrer un jeu de requête afin d'avoir l'ensemble le plus pertinent pour notre benchmark
 */
public class QueriesFilter {
    static Set<String> queriesWithAnswers, queriesWithoutAnswers, queriesWithMostConditions;
    static List<String> queriesWithAnswersDuplicated, queriesWithoutAnswersDuplicated, queriesWithMostConditionsDuplicated;
    private final static int MOST_CONDITIONS = 3;
    private final static double RETAIN_PERCENTAGE = .2;
    private final static double NO_ANSWERS_PERCENTAGE = .05;

    public static void main(String[] args) throws Exception {
        JCommander.newBuilder()
                .addObject(new Main())
                .build()
                .parse(args);

        // initialisation des différents jeux de requêtes
        queriesWithAnswers = new HashSet<>();
        queriesWithoutAnswers = new HashSet<>();
        queriesWithMostConditions = new HashSet<>();
        queriesWithAnswersDuplicated = new ArrayList<>();
        queriesWithoutAnswersDuplicated = new ArrayList<>();
        queriesWithMostConditionsDuplicated = new ArrayList<>();

        Main.parseData();

        Map<ParsedQuery, String> queries = Main.parseQueriesFolder();

        for (Map.Entry<ParsedQuery, String> entry : queries.entrySet()) {
            Set<Integer> answers = Main.processAQuery(entry.getKey());

            addToList(entry, answers);
        }

        saveToFile(queriesWithAnswers, "answers");
        saveToFile(queriesWithoutAnswers, "no_answers");
        saveToFile(queriesWithMostConditions, "most_conditions");
        saveToFile(queriesWithAnswersDuplicated, "answers_duplicated");
        saveToFile(queriesWithoutAnswersDuplicated, "no_answers_duplicated");
        saveToFile(queriesWithMostConditionsDuplicated, "most_conditions_duplicated");

        if(Main.verbose) {
            int total = queriesWithAnswers.size() + queriesWithoutAnswers.size() + queriesWithMostConditions.size() +
                        queriesWithAnswersDuplicated.size() + queriesWithoutAnswersDuplicated.size() +
                        queriesWithMostConditionsDuplicated.size();

            System.out.println("Nb total de requêtes (original) : " + queries.size());
            System.out.println("Nb de requêtes avec réponses : " + queriesWithAnswers.size());
            System.out.println("Nb de requêtes sans réponses : " + queriesWithoutAnswers.size());
            System.out.println("Nb de requêtes avec plus de "+ MOST_CONDITIONS +" conditions : " + queriesWithMostConditions.size());
            System.out.println("Nb de requêtes dupliquées avec réponses : " + queriesWithAnswersDuplicated.size());
            System.out.println("Nb de requêtes dupliquées sans réponses : " + queriesWithoutAnswersDuplicated.size());
            System.out.println("Nb de requêtes dupliquées avec plus de "+ MOST_CONDITIONS +" conditions : " + queriesWithMostConditionsDuplicated.size());
            System.out.println("Nb total de requêtes (final) : " + total);
        }
    }

    /**
     * Ajoute une requête dans un des jeux de requêtes en fonction des résultats
     * @param entry la requête
     * @param answers la/les réponses (peut être vide) à la requête
     */
    public static void addToList(Map.Entry<ParsedQuery, String> entry, Set<Integer> answers) {
        ParsedQuery query = entry.getKey();
        String queryString = entry.getValue();
        List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());
        double retainPercentage = Math.random();

        if(answers.size() > 0) {
            if(retainPercentage <= RETAIN_PERCENTAGE) {
                if (patterns.size() > MOST_CONDITIONS && !queriesWithMostConditions.contains(queryString) && !queriesWithMostConditionsDuplicated.contains(queryString)) {
                    queriesWithMostConditionsDuplicated.add(queryString);
                    queriesWithMostConditionsDuplicated.add(queryString);
                } else if(!queriesWithAnswers.contains(queryString) && !queriesWithAnswersDuplicated.contains(queryString)) {
                    queriesWithAnswersDuplicated.add(queryString);
                    queriesWithAnswersDuplicated.add(queryString);
                }
            } else {
                if(patterns.size() > MOST_CONDITIONS && !queriesWithMostConditionsDuplicated.contains(queryString))
                    queriesWithMostConditions.add(queryString);
                else if(!queriesWithAnswersDuplicated.contains(queryString))
                    queriesWithAnswers.add(queryString);
            }
        } else if(retainPercentage <= NO_ANSWERS_PERCENTAGE && !queriesWithoutAnswers.contains(queryString) && !queriesWithoutAnswersDuplicated.contains(queryString)) {
            double duplicatePercentage = Math.random();
            if (duplicatePercentage <= RETAIN_PERCENTAGE) {
                queriesWithoutAnswersDuplicated.add(queryString);
                queriesWithoutAnswersDuplicated.add(queryString);
            } else
                queriesWithoutAnswers.add(queryString);
        }
    }

    /**
     * créé des fichiers pour chaque jeu de requêtes dans le dossier filtered/ à la racine du projet
     * @param queries requêtes à sauvegarder
     * @param filename nom du fichier final
     */
    public static void saveToFile(Collection<String> queries, String filename) {
        try {
            File file = new File("filtered/" + filename + ".queryset");
            file.createNewFile();
            FileWriter myWriter = new FileWriter("filtered/" + filename + ".queryset");
            myWriter.write(String.join("\n", queries));
            myWriter.close();
        } catch (Exception e) {
            System.err.println("Une erreur est survenue...");
        }
    }

}
