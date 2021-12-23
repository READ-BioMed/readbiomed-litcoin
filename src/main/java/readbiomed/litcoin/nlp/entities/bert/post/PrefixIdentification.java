package readbiomed.litcoin.nlp.entities.bert.post;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.cleartk.token.type.Token;

public class PrefixIdentification extends JCasAnnotator_ImplBase {

	private Map<String, String> mappingType = new HashMap<>();

	private Map<String, Set<String>> typeTerms = new HashMap<>();

	public void initialize(UimaContext context) throws ResourceInitializationException {
		mappingType.put("CellLine", "CellLine");
		mappingType.put("Chemical", "ChemicalEntity");
		mappingType.put("DNAMutation", "SequenceVariant");
		mappingType.put("Disease", "DiseaseOrPhenotypicFeature");
		mappingType.put("Gene", "GeneOrGeneProduct");
		mappingType.put("ProteinMutation", "SequenceVariant");
		mappingType.put("SNP", "SequenceVariant");
		mappingType.put("Mutation", "SequenceVariant");
		mappingType.put("Species", "OrganismTaxon");

		try (BufferedReader b = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				new FileInputStream("/Users/ajimeno/Documents/UoM/LitCoin/dictionaries/dict.csv.gz"))))) {

			Pattern pHyphen = Pattern.compile("-");
			Pattern pPipe = Pattern.compile("\\|");

			for (String line; (line = b.readLine()) != null;) {
				if (!line.endsWith("|")) {

					try {
						String type = mappingType.get(pHyphen.split(line.substring(8))[0]);
						String term = pPipe.split(line.substring(8))[1].toLowerCase();

						Set<String> terms = typeTerms.get(type);

						if (terms == null) {
							terms = new HashSet<>();
							typeTerms.put(type, terms);
						}

						terms.add(term);
					} catch (Exception e) {
						System.out.println(line);
						throw new ResourceInitializationException(e);
					}
				}
			}
		} catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

		System.out.println(typeTerms.size());
		System.out.println(typeTerms.keySet());
		for (Map.Entry<String, Set<String>> e : typeTerms.entrySet()) {
			System.out.println(e.getKey() + "|" + e.getValue().size());
		}
	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(PrefixIdentification.class);
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		// List<Token> tokens = new ArrayList<>(JCasUtil.select(jCas, Token.class));

		for (NamedEntityMention ne : JCasUtil.select(jCas, NamedEntityMention.class)) {

			if (ne.getMentionType().equals("GeneOrGeneProduct")) {

				List<Token> edgeToken = JCasUtil.selectCovered(jCas, Token.class, ne);

				if (edgeToken != null && edgeToken.size() > 0) {
					Token left = edgeToken.get(0);

					// Find token on the left
					List<Token> ltoken = JCasUtil.selectPreceding(jCas, Token.class, left, 1);

					while (ltoken != null && ltoken.size() > 0) {
						left = ltoken.get(0);

						if (left.getCoveredText().equals("and")) {
							break;
						}

						String term = jCas.getDocumentText().substring(left.getBegin(), ne.getEnd());

						if (!term.startsWith(",") && !term.startsWith(")") && !term.startsWith("(")
								&& !term.startsWith("in")) {
							Set<String> terms = typeTerms.get(ne.getMentionType());

							if (terms != null) {
								if (terms.contains(term)) {
									if (left.getBegin() < ne.getBegin()) {
										ne.setBegin(left.getBegin());
										System.out.println(ne);
										System.out.println(ne.getCoveredText());
									}
								}
							}
						}
						ltoken = JCasUtil.selectPreceding(jCas, Token.class, left, 1);
					}
				}
			}
		}
	}
}