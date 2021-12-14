package readbiomed.litcoin.nlp.entities.chemicals;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.conceptMapper.DictTerm;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;

import readbiomed.litcoin.nlp.entities.utils.Utils;

public class ChemicalAnnotator extends JCasAnnotator_ImplBase {

	private Set<String> filterWords = new HashSet<>();

	public void initialize(UimaContext context) throws ResourceInitializationException {
		try {
			filterWords = Utils.getStopwords("readbiomed/litcoin/nlp/entities/chemicals/stopwords.txt");
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		JCasUtil.select(jCas, DictTerm.class).stream().filter(e -> e.getDictCanon().startsWith("chemical-"))
				.forEach(e -> {
					if (!filterWords.contains((e.getCoveredText().toLowerCase()))) {
						NamedEntityMention n = new NamedEntityMention(jCas);
						n.setBegin(e.getBegin());
						n.setEnd(e.getEnd());
						n.setMentionId(e.getDictCanon());
						n.setMentionType("ChemicalEntity");
						n.addToIndexes(jCas);
					}
				});
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(ChemicalAnnotator.class);
	}
}