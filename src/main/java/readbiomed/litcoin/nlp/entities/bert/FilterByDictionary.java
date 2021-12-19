package readbiomed.litcoin.nlp.entities.bert;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;

public class FilterByDictionary extends JCasAnnotator_ImplBase {

	private Map<String, String> mappingType = new HashMap<>();

	private Map<String, String> termType = new HashMap<>();

	public void initialize(UimaContext context) throws ResourceInitializationException {
		mappingType.put("CellLine", "CellLine");
		mappingType.put("Chemical", "ChemicalEntity");
		mappingType.put("DNAMutation", "SequenceVariant");
		mappingType.put("Disease", "DiseaseOrPhenotypicFeature");
		mappingType.put("Gene", "GeneOrGeneProduct");
		mappingType.put("ProteinMutation", "SequenceVariant");
		mappingType.put("SNP", "SequenceVariant");
		mappingType.put("Species", "OrganismTaxon");

		try (BufferedReader b = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				new FileInputStream("/Users/ajimeno/Documents/UoM/LitCoin/dictionaries/dict.csv.gz"))))) {

			Pattern pHyphen = Pattern.compile("-");
			Pattern pPipe = Pattern.compile("\\|");

			Map<String, Integer> termCount = new HashMap<>();

			for (String line; (line = b.readLine()) != null;) {
				if (!line.endsWith("|")) {
					int count = Integer.parseInt(line.substring(0, 7).trim());

					try {
						String type = pHyphen.split(line.substring(8))[0];
						String term = pPipe.split(line.substring(8))[1].toLowerCase();

						if (termType.get(term) == null) {
							termType.put(term, mappingType.get(type));
							termCount.put(term, count);
						} else {
							if (termCount.get(term) < count) {
								termType.put(term, mappingType.get(type));
								termCount.put(term, count);
							}
						}
					} catch (Exception e) {
						System.out.println(line);
						throw new ResourceInitializationException(e);
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		/*
		 * JCasUtil.select(jCas, NamedEntityMention.class).stream().forEach(e -> { if
		 * (termType.get(e.getCoveredText().toLowerCase()) != null &&
		 * termType.get(e.getCoveredText().toLowerCase()).equals("ChemicalEntity")) {
		 * 
		 * if
		 * (!e.getMentionType().equals(termType.get(e.getCoveredText().toLowerCase())))
		 * System.out.println(e.getCoveredText() + "|" + e.getMentionType() + "|" +
		 * termType.get(e.getCoveredText().toLowerCase()));
		 * 
		 * e.setMentionType(termType.get(e.getCoveredText().toLowerCase())); } });
		 */

		Set<NamedEntityMention> remove = new HashSet<>();

		JCasUtil.select(jCas, NamedEntityMention.class).stream().forEach(e -> {
			if (termType.get(e.getCoveredText().toLowerCase()) == null) {
				remove.add(e);
			}
		});
		
		remove.stream().forEach(e->e.removeFromIndexes());
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(FilterByDictionary.class);
	}
}