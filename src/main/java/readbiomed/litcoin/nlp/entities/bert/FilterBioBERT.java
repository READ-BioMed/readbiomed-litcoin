package readbiomed.litcoin.nlp.entities.bert;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;

public class FilterBioBERT extends JCasAnnotator_ImplBase {

	private boolean checkNumber(String term) {
		try {
			Double.parseDouble(term);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private boolean checkTerm(String term) {
		if (term.length() < 2) {
			return false;
		}

		if ((term.startsWith(":") || term.startsWith("\"") || term.startsWith("/"))
				|| (term.endsWith(":") || term.endsWith("\"") || term.endsWith("/"))) {
			return false;
		}

		return !checkNumber(term);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		List<NamedEntityMention> removeNEM = new ArrayList<>();

		JCasUtil.select(jCas, NamedEntityMention.class).stream().forEach(e -> {
			if ((e.getCoveredText().contains("(") && !e.getCoveredText().contains(")"))
					|| (e.getCoveredText().contains(")") && !e.getCoveredText().contains("("))
					|| !checkTerm(e.getCoveredText().toLowerCase())
					|| (e.getCoveredText().startsWith("-") && !e.getMentionType().startsWith("Sequence"))) {
				removeNEM.add(e);
			}
		});

		removeNEM.stream().forEach(e -> e.removeFromIndexes());
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(FilterBioBERT.class);
	}
}