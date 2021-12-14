package readbiomed.litcoin.nlp.entities.genes;

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

public class GeneAnnotator extends JCasAnnotator_ImplBase {

	private Set<String> filterWords = new HashSet<>();

	public void initialize(UimaContext context) throws ResourceInitializationException {
		try {
			filterWords = Utils.getStopwords("readbiomed/litcoin/nlp/entities/genes/stopwords.txt");
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	private boolean checkNumber(String term) {
		try {
			Double.parseDouble(term);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private boolean checkTerm(String term) {
		if (term.startsWith("or ") || term.startsWith("or, ") || term.startsWith("a ") || term.startsWith("at ")
				|| term.startsWith("as ") || term.startsWith("to ")) {
			return false;
		}

		if (term.charAt(0) >= '0' && term.charAt(0) <= '9') {
			return false;
		}

		return !checkNumber(term);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		JCasUtil.select(jCas, DictTerm.class).stream().filter(e -> e.getDictCanon().startsWith("gene-")).forEach(e -> {
			if (!(e.getCoveredText().contains("(") && !e.getCoveredText().contains(")"))) {
				if (!filterWords.contains((e.getCoveredText().toLowerCase()))) {
					if (checkTerm(e.getCoveredText().toLowerCase())) {
						NamedEntityMention n = new NamedEntityMention(jCas);
						n.setBegin(e.getBegin());
						n.setEnd(e.getEnd());
						n.setMentionId(e.getDictCanon());
						n.setMentionType("GeneOrGeneProduct");
						n.addToIndexes(jCas);
					}
				}
			}
		});

	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(GeneAnnotator.class);
	}
}