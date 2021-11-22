package qengine.program;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import qengine.program.dictionary.Dictionary;
import qengine.program.index.Index;
import qengine.program.index.Type;

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
		String subject = st.getSubject().stringValue();
		String predicate = st.getPredicate().stringValue();
		String object = st.getObject().stringValue();
		Dictionary dictionary = Dictionary.getInstance();

		// Ajout dans le dictionnaire
		dictionary.add(subject, predicate, object);

		// Ajout dans les index
		int S = dictionary.get(subject);
		int P = dictionary.get(predicate);
		int O = dictionary.get(object);

		for(Type type : Type.values()) {
			Index.getInstance(type).add(S, P, O);
		}
	}
}