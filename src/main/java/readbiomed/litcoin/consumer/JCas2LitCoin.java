package readbiomed.litcoin.consumer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.util.ViewUriUtil;

public class JCas2LitCoin extends JCasAnnotator_ImplBase {
	public static final String PARAM_OUTPUT_FOLDER_NAME = "outputFolderName";

	private String outputFolderName = "";

	private BufferedWriter w;

	private Map<String, String> typeMapping = new HashMap<String, String>();

	public void initialize(UimaContext context) throws ResourceInitializationException {
		outputFolderName = (String) context.getConfigParameterValue(PARAM_OUTPUT_FOLDER_NAME);

		typeMapping.put("Mutation", "SequenceVariant");

		try {
			w = new BufferedWriter(new FileWriter(new File(outputFolderName, "submission_readbiomed_entities.csv")));

			w.write("id\tabstract_id\toffset_start\toffset_finish\ttype\n");
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	private String getTypeMapping(String type) {
		String mapping = typeMapping.get(type);
		return (mapping != null ? mapping : type);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		int nid = 0;
		String abstractId = ViewUriUtil.getURI(jCas).toString();
		
		// Debugging code
		for (Annotation a : JCasUtil.select(jCas,  Annotation.class)) {
			System.out.println(a);
			System.out.println(a.getCoveredText());
			System.out.println("----");
		}

		// Go over the entities
		for (NamedEntityMention ne : JCasUtil.select(jCas, NamedEntityMention.class)) {
			try {
				w.write(Integer.toString(nid));
				w.write("\t");
				w.write(abstractId);
				w.write("\t");
				w.write(Integer.toString(ne.getBegin()));
				w.write("\t");
				w.write(Integer.toString(ne.getEnd()));
				w.write("\t");
				w.write(getTypeMapping(ne.getMentionType()));

				// For debugging purposes
				w.write("\t");
				w.write(ne.getCoveredText());
				w.write("\t");
				if (ne.getMentionId() != null)
					w.write(ne.getMentionId());

				w.write("\n");

				w.flush();
			} catch (IOException e) {
				throw new AnalysisEngineProcessException(e);
			}

			nid++;
		}

		// Go over the relations - for the second part of the challenge
		// JCasUtil.select(jCas, Relation.class).stream().forEach(r -> {
		// System.out.println(r);
		// });
	}

	public static AnalysisEngineDescription getOutputFolderDescription(String outputFolderName)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(JCas2LitCoin.class, PARAM_OUTPUT_FOLDER_NAME,
				outputFolderName);
	}

	@Override
	protected void finalize() throws Throwable {
		w.close();
	}
}