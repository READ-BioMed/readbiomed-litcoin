package readbiomed.litcoin.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.util.ViewUriUtil;

import readbiomed.document.Section;

public class PubTatorReader extends JCasCollectionReader_ImplBase {

	public static final String PARAM_FILE_NAME = "fileName";

	private static Pattern pTab = Pattern.compile("\t");
	private static Pattern pPipe = Pattern.compile("\\|");

	private ArrayDeque<String> documents = new ArrayDeque<>();

	private Map<String, String> title = new HashMap<>();
	private Map<String, String> abstractText = new HashMap<>();
	private Map<String, List<String>> entityLines = new HashMap<>();

	private Map<String, String> mappingType = new HashMap<>();

	public void initialize(UimaContext context) throws ResourceInitializationException {
		mappingType.put("CellLine", "CellLine");
		mappingType.put("Chemical", "ChemicalEntity");
		mappingType.put("DNAMutation", "SequenceVariant");
		mappingType.put("Disease", "DiseaseOrPhenotypicFeature");
		mappingType.put("Gene", "GeneOrGeneProduct");
		mappingType.put("ProteinMutation", "SequenceVariant");
		mappingType.put("SNP", "SequenceVariant");
		mappingType.put("Species", "OrganismTaxon");

		String fileName = (String) context.getConfigParameterValue(PARAM_FILE_NAME);

		try (BufferedReader b = new BufferedReader(new FileReader(fileName))) {
			for (String line; (line = b.readLine()) != null;) {
				if (line.trim().length() > 0) {

					String[] tokens = pPipe.split(line);

					if (tokens.length == 3) {
						if (!documents.contains(tokens[0])) {
							documents.add(tokens[0]);
						}

						if (tokens[1].equals("t")) {
							title.put(tokens[0], tokens[2].trim());
						} else if (tokens[1].equals("a")) {
							abstractText.put(tokens[0], tokens[2].replaceAll("^\"", "").replaceAll("\"$", "")
									.replaceAll("\"\"", "\"").trim());
						}
					} else {
						String[] tokensPipe = pTab.split(line);
						if (tokensPipe.length == 6) {
							List<String> lines = entityLines.get(tokensPipe[0]);

							if (lines == null) {
								lines = new ArrayList<>();
								entityLines.put(tokensPipe[0], lines);
							}
							lines.add(line);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return documents.size() > 0;
	}

	@Override
	public Progress[] getProgress() {
		return null;
	}

	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		String document = documents.pop();

		try {
			ViewUriUtil.setURI(jCas, new URI(document));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		String text = title.get(document) + " "
				+ (abstractText.get(document) == null ? "" : abstractText.get(document));
		jCas.setDocumentText(text);

		Section titleSection = new Section(jCas);
		titleSection.setSectionType("title");
		titleSection.setBegin(0);
		titleSection.setEnd(title.get(document).length());
		titleSection.addToIndexes(jCas);

		if (abstractText.get(document) != null) {
			Section abstractSection = new Section(jCas);
			abstractSection.setSectionType("abstract");
			abstractSection.setBegin(title.get(document).length() + 1);
			abstractSection.setEnd(title.get(document).length() + 1 + abstractText.get(document).length());
			abstractSection.addToIndexes(jCas);
		}

		// Add the entities
		if (entityLines.get(document) != null) {
			entityLines.get(document).stream().forEach(e -> {
				String[] tokens = pTab.split(e);

				NamedEntityMention ne = new NamedEntityMention(jCas);
				ne.setBegin(Integer.parseInt(tokens[1]));
				ne.setEnd(Integer.parseInt(tokens[2]));
				ne.setMentionType(mappingType.get(tokens[4]));
				ne.addToIndexes();
			});
		}
	}

	public static CollectionReaderDescription getDescriptionFromFiles(String fileName)
			throws ResourceInitializationException {
		return CollectionReaderFactory.createReaderDescription(PubTatorReader.class, null, PARAM_FILE_NAME, fileName);
	}

}