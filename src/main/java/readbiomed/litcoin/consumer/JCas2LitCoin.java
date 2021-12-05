package readbiomed.litcoin.consumer;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;

import com.ibm.au.research.nlp.types.Relation;

public class JCas2LitCoin extends JCasAnnotator_ImplBase {
	public static final String PARAM_OUTPUT_FOLDER_NAME = "outputFolderName";

	private String outputFolderName = "";

	public void initialize(UimaContext context) throws ResourceInitializationException {
		outputFolderName = (String) context.getConfigParameterValue(PARAM_OUTPUT_FOLDER_NAME);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// Go over the entities
		JCasUtil.select(jCas, NamedEntityMention.class).stream().forEach(e -> {
			System.out.println(e);
		});

		// Go over the relations
		JCasUtil.select(jCas, Relation.class).stream().forEach(r -> {
			System.out.println(r);
		});
	}

	public static AnalysisEngineDescription getOutputFolderDescription(String outputFolderName)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(JCas2LitCoin.class, PARAM_OUTPUT_FOLDER_NAME,
				outputFolderName);
	}
}