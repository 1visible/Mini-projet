package qengine.program;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import qengine.program.dictionary.Dictionary;
import qengine.program.index.Index;
import qengine.program.index.Type;
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
final class Main {
	static final String baseURI = null;
	/**
	 * Fichier contenant les requêtes sparql
	 */
	@Parameter(names={"-queries"})
	static String queryDirectory = "data/";

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

	// ========================================================================

	/**
	 * Méthode utilisée ici lors du parsing de requête sparql pour agir sur l'objet obtenu.
	 */
	public static void processAQuery(ParsedQuery query) {
		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

		Dictionary dictionary = Dictionary.getInstance();
		List<Set<Integer>> result = new ArrayList<>();
		Set<Integer> intersection = new HashSet<>();

		try {
			for (StatementPattern pattern : patterns) {
				// On récupère les valeurs SPO brutes
				Value subject = pattern.getSubjectVar().getValue();
				Value predicate = pattern.getPredicateVar().getValue();
				Value object = pattern.getObjectVar().getValue();
				Set<Integer> indexes;

				// On identifie quelle est la valeur à chercher (celle qui n'a pas de valeur)
				if(subject == null) {
					// On récupère les index associés
					int P = dictionary.get(predicate.toString());
					int O = dictionary.get(object.toString());

					// On stocke le résultat de la recherche dans 'indexes'
					indexes = (P < O) ?
							Index.getInstance(Type.POS).search(P, O) :
							Index.getInstance(Type.OPS).search(O, P);
				} else if(predicate == null) {
					int S = dictionary.get(subject.toString());
					int O = dictionary.get(object.toString());

					indexes = (S < O) ?
							Index.getInstance(Type.SOP).search(S, O) :
							Index.getInstance(Type.OSP).search(O, S);
				} else {
					int S = dictionary.get(subject.toString());
					int P = dictionary.get(predicate.toString());

					indexes = (S < P) ?
							Index.getInstance(Type.SPO).search(S, P) :
							Index.getInstance(Type.PSO).search(P, S);
				}
				result.add(indexes);
			}
		} catch(NullPointerException ignored) { }

		// Fais l'intersection
		try {
			if(result.size() > 0) {
				intersection = new HashSet<>(result.get(0));

				for (Set<Integer> list : result) {
					intersection.retainAll(list);
				}
			}

			if (intersection.size() == 0) {
				System.out.println("Aucune valeur trouvée...");
				Monitor.append(Field.QUERIES_WITHOUT_RESULT_COUNT, 1);
			}

			/*
			for (Integer index : intersection) {
				System.out.println(dictionary.get(index));
			}
			 */

		} catch (NullPointerException e) {
			System.out.println("Aucune valeur trouvée...");
			Monitor.append(Field.QUERIES_WITHOUT_RESULT_COUNT, 1);
		}
	}

	/**
	 * Entrée du programme
	 */
	public static void main(String[] args) throws Exception {
		Timer.start(Watch.TOTAL);

		JCommander.newBuilder()
				.addObject(new Main())
				.build()
				.parse(args);

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
			// System.out.println("\n" + query.getValue());
			processAQuery(query.getKey());
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

	/**
	 * Traite chaque requête lue dans {@link #queryDirectory} avec {@link #processAQuery(ParsedQuery)}.
	 */

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
