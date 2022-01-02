package qengine.program;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import qengine.program.monitor.Field;
import qengine.program.monitor.Monitor;
import qengine.program.timer.Watch;
import qengine.program.timer.Timer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Programme simple lisant un fichier de requête et un fichier de données.
 *
 * <p>
 * Les entrées sont données ici de manière statique,
 * à vous de programmer les entrées par passage d'arguments en ligne de commande comme demandé dans l'énoncé.
 * </p>
 *
 * <p>
 * Le présent programme se contente de vous montrer la voie pour lire les triples et requêtes
 * depuis les fichiers ; ce sera à vous d'adapter/réécrire le code pour finalement utiliser les requêtes et interroger les données.
 * On ne s'attend pas forcémment à ce que vous gardiez la même structure de code, vous pouvez tout réécrire.
 * </p>
 *
 * @author Olivier Rodriguez <olivier.rodriguez1@umontpellier.fr>
 */
final class MainJena {
    static final String baseURI = null;
    /**
     * Fichier contenant les requêtes sparql
     */
    @Parameter(names={"-queries"})
    static String queryDirectory = "data/watdiv/Q_1_eligibleregion.queryset";

    /**
     * Fichier contenant des données rdf
     */
    @Parameter(names={"-data"})
    static String dataFile = "data/100K.nt";

    /**
     * Fichier contenant les résultats à exporter
     */
    @Parameter(names={"-output"})
    static String outputFile = "data/sample_output.csv";

    /**
     * Booléen vérifiant si on affiche les résultats ou non
     */
    @Parameter(names={"-verbose"})
    static boolean verbose = true;

    static List<String> result = new ArrayList<>();

    // ========================================================================


    /**
     * Entrée du programme
     */
    public static void main(String[] args) throws Exception {
        Timer.start(Watch.TOTAL);

        JCommander.newBuilder()
                .addObject(new Main())
                .build()
                .parse(args);

        Model model = ModelFactory.createDefaultModel();
        model.read(dataFile);

        Monitor.append(Field.DATA_FILENAME, dataFile);
        Monitor.append(Field.QUERIES_FILENAME, queryDirectory);
        Timer.start(Watch.DICT_CREATION);

        parseData();

        Timer.stop(Watch.DICT_CREATION);

        Monitor.append(Field.INDEX_COUNT, Field.RDF_TRIPLES_COUNT.getNumValue() * 6);
        Timer.start(Watch.QUERIES_READ);

        Map<ParsedQuery, String> queries = parseQueriesFolder();

        Timer.stop(Watch.QUERIES_READ);
        Monitor.append(Field.QUERIES_COUNT, queries.size());
        Timer.start(Watch.WORKLOAD);

        for (Map.Entry<ParsedQuery, String> query : queries.entrySet()) {

            QueryExecution execution = QueryExecutionFactory.create(query.getValue(), model);
            StringBuilder st = new StringBuilder();
            st.append(query.getValue());
            try {
                ResultSet rs = execution.execSelect();
                List<QuerySolution> solution = ResultSetFormatter.toList(rs);
                if(solution.isEmpty()) st.append("\naucune Solution");
                for (QuerySolution querySolution : solution) {
                    querySolution.varNames().forEachRemaining((varName) -> {
                        st.append(querySolution.get(varName)).append("\n");
                    });
                }
            } finally {
                execution.close();
            }
            result.add(st.toString());
        }

        if(verbose)
            for (String re : result) {
                System.out.println(re);
            }

        Timer.stop(Watch.WORKLOAD);
        Timer.stop(Watch.TOTAL);

        Monitor.append(Field.DATA_READ_TIME, Timer.get(Watch.DATA_READ));
        Monitor.append(Field.DICT_CREATION_TIME, Timer.get(Watch.DICT_CREATION));
        Monitor.append(Field.QUERIES_READ_TIME, Timer.get(Watch.QUERIES_READ));
        Monitor.append(Field.IND_CREATION_TIME, Timer.get(Watch.IND_CREATION));
        Monitor.append(Field.WORKLOAD_TIME, Timer.get(Watch.WORKLOAD));
        Monitor.append(Field.TOTAL_TIME, Timer.get(Watch.TOTAL));

        System.out.println(new Monitor());
        Monitor.writeToCsv(outputFile);

		/*

		System.out.println("==================\n\tDictionary\n==================\n");
		System.out.println(Dictionary.getInstance());

		for(Type type : Type.values()) {
			System.out.println("==================\n\tIndex " + type.name() + "\n==================\n");
			System.out.println(Index.getInstance(type));
		}

		 */
    }

    private static Map<ParsedQuery, String> parseQueriesFolder() throws IOException {
        Map<ParsedQuery, String> queries = new HashMap<>();

        File folder = new File(queryDirectory);

        if(!folder.exists())
            throw new NoSuchFileException("Le dossier n'existe pas !");

        if(folder.isFile()) {

            if(!FilenameUtils.getExtension(folder.getName()).equals("queryset"))
                throw new NoSuchFileException("Le fichier n'a pas la bonne extension");

            Map<ParsedQuery, String> queriesFile = parseQueries(folder.getAbsolutePath());
            queries.putAll(queriesFile);
        } else if(folder.isDirectory() && folder.listFiles() != null) {
            for(File file : folder.listFiles()) {

                if(!FilenameUtils.getExtension(file.getName()).equals("queryset"))
                    continue;

                Map<ParsedQuery, String> queriesFile = parseQueries(file.getAbsolutePath());
                queries.putAll(queriesFile);
            }
        } else
            throw new NoSuchFileException("Le dossier n'existe pas !");

        return queries;
    }

    private static Map<ParsedQuery, String> parseQueries(String queryFile) throws IOException {
        /*
         * On utilise un stream pour lire les lignes une par une, sans avoir à toutes les stocker
         * entièrement dans une collection.
         */
        Map<ParsedQuery, String> queries = new HashMap<>();

        try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
            SPARQLParser sparqlParser = new SPARQLParser();
            Iterator<String> lineIterator = lineStream.iterator();
            StringBuilder queryString = new StringBuilder();

            while (lineIterator.hasNext())
                /*
                 * On stocke plusieurs lignes jusqu'à ce que l'une d'entre elles se termine par un '}'
                 * On considère alors que c'est la fin d'une requête
                 */
            {
                String line = lineIterator.next();
                queryString.append(line);

                if (line.trim().endsWith("}")) {
                    ParsedQuery query = sparqlParser.parseQuery(queryString.toString(), baseURI);

                    // Ajout de la requête à la liste des requêtes à traiter
                    queries.put(query, queryString.toString().trim());

                    queryString.setLength(0); // Reset le buffer de la requête en chaine vide
                }
            }
        }

        return queries;
    }

    /**
     * Traite chaque triple lu dans {@link #dataFile} avec {@link MainRDFHandler}.
     */
    private static void parseData() throws IOException {

        try (Reader dataReader = new FileReader(dataFile)) {
            // On va parser des données au format ntriples
            RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);

            // On utilise notre implémentation de handler
            rdfParser.setRDFHandler(new MainRDFHandler());

            // Parsing et traitement de chaque triple par le handler
            rdfParser.parse(dataReader, baseURI);
        }
    }
}
