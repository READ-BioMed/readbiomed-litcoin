package readbiomed.litcoin.nlp.entities.bert;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;

public class FilterNestedEntities extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// For each NamedEntityMention

		Set<NamedEntityMention> removeNEs = new HashSet<>();

		for (NamedEntityMention namedEntity : JCasUtil.select(jCas, NamedEntityMention.class)) {
			if (!removeNEs.contains(namedEntity)) {
				for (NamedEntityMention nestedNamedEntity : JCasUtil.selectCovered(jCas, NamedEntityMention.class,
						namedEntity.getBegin(), namedEntity.getEnd())) {
					if (namedEntity != nestedNamedEntity) {
						removeNEs.add(nestedNamedEntity);
					}
				}
			}
		}

		for (NamedEntityMention removeNE : removeNEs) {
			removeNE.removeFromIndexes(jCas);
		}

	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(FilterNestedEntities.class);
	}
}