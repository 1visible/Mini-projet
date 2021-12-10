package qengine.program;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import qengine.program.dictionary.Dictionary;
import qengine.program.index.Index;
import qengine.program.index.Type;
import qengine.program.monitor.Field;
import qengine.program.monitor.Monitor;
import qengine.program.timer.Timer;
import qengine.program.timer.Watch;

/**
 * Le RDFHandler intervient lors du parsing de données et permet d'appliquer un traitement pour chaque élément lu par le parseur.
 * 
 * <p>
 * Ce qui servira surtout dans le programme est la méthode {@link #handleStatement(Statement)} qui va permettre de traiter chaque triple lu.
 * </p>
 * <p>
 * À adapter/réécrire selon vos traitements.
 * </p>
 */
public final class 	MainRDFHandler extends AbstractRDFHandler {

	@Override
	public void handleStatement(Statement st) {
		Monitor.append(Field.RDF_TRIPLES_COUNT, 1);
		Timer.start(Watch.DATA_READ);

		String subject = st.getSubject().stringValue();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().stringValue();
		Dictionary dictionary = Dictionary.getInstance();

		Timer.stop(Watch.DATA_READ);

		// Ajout dans le dictionnaire
		dictionary.add(subject, predicate, object);

		// Ajout dans les index
		int S = dictionary.get(subject);
		int P = dictionary.get(predicate);
		int O = dictionary.get(object);

		for(Type type : Type.values()) {
			Timer.start(Watch.IND_CREATION);
			Index.getInstance(type).add(S, P, O);
			Timer.stop(Watch.IND_CREATION);
		}
	}
}