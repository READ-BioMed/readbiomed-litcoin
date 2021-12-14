package readbiomed.litcoin.nlp.entities.mutations;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;

public class MutationFiltering extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		Set<NamedEntityMention> removeNEs = new HashSet<>();

		for (NamedEntityMention namedEntity : JCasUtil.select(jCas, NamedEntityMention.class)) {
			if (namedEntity.getMentionType().equals("Mutation")) {
				if (namedEntity.getCoveredText().equals("-/-") || namedEntity.getCoveredText().equals("+/-")) {
					removeNEs.add(namedEntity);
				}
			}
		}

		for (NamedEntityMention removeNE : removeNEs) {
			removeNE.removeFromIndexes(jCas);
		}

	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(MutationFiltering.class);
	}
}