package readbiomed.litcoin.nlp.entities.bert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.util.ViewUriUtil;

public class Litcoin2JCas extends JCasAnnotator_ImplBase {

	public static final String PARAM_INPUT_FILE_NAME = "inputFileName";

	private Map<String, List<String>> map = new HashMap<>();

	Pattern p = Pattern.compile("\t");

	public void initialize(UimaContext context) throws ResourceInitializationException {

		String inputFileName = (String) context.getConfigParameterValue(PARAM_INPUT_FILE_NAME);

		try (BufferedReader b = new BufferedReader(new FileReader(inputFileName))) {
			// Skip first line
			b.readLine();

			for (String line; (line = b.readLine()) != null;) {
				String[] tokens = p.split(line);

				List<String> list = map.get(tokens[1]);

				if (list == null) {
					list = new ArrayList<>();
					map.put(tokens[1], list);
				}

				list.add(line);
			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		String pmid = ViewUriUtil.getURI(jCas).toString();

		if (map.get(pmid) != null)
			map.get(pmid).stream().forEach(e -> {
				String[] tokens = p.split(e);

				NamedEntityMention ne = new NamedEntityMention(jCas);
				ne.setBegin(Integer.parseInt(tokens[2]));
				ne.setEnd(Integer.parseInt(tokens[3]));
				ne.setMentionType(tokens[4]);
				ne.addToIndexes();
			});
	}

	public static AnalysisEngineDescription getInputFileDescription(String inputFileName)
			throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(Litcoin2JCas.class, PARAM_INPUT_FILE_NAME, inputFileName);
	}
}