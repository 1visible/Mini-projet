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

		System.out.println("\n" + subject + "\t " + predicate + "\t " + object);

		// Ajout dans le dictionnaire
		dictionary.add(subject, predicate, object);

		// Ajout dans les index
		int s = dictionary.get(subject);
		int p = dictionary.get(predicate);
		int o = dictionary.get(object);

		Index.getInstance(Type.OPS).add(s, p, o);
		Index.getInstance(Type.OSP).add(s, p, o);
		Index.getInstance(Type.POS).add(s, p, o);
		Index.getInstance(Type.PSO).add(s, p, o);
		Index.getInstance(Type.SOP).add(s, p, o);
		Index.getInstance(Type.SPO).add(s, p, o);
	}
}