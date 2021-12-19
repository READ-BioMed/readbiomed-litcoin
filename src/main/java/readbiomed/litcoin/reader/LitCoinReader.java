package readbiomed.litcoin.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.cleartk.util.ViewUriUtil;

import readbiomed.document.Section;

public class LitCoinReader extends JCasCollectionReader_ImplBase {

	public static final String PARAM_FILE_NAME = "fileName";

	private ArrayDeque<String> documents = new ArrayDeque<>();

	private static Pattern p = Pattern.compile("\t");

	public void initialize(UimaContext context) throws ResourceInitializationException {
		String fileName = (String) context.getConfigParameterValue(PARAM_FILE_NAME);

		try (BufferedReader b = new BufferedReader(new FileReader(fileName))) {
			// Remove first line
			b.readLine();
			for (String line; (line = b.readLine()) != null;) {
				String[] tokens = p.split(line);

				if (tokens.length == 3) {
					documents.add(line);
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
		String[] tokens = p.split(documents.pop());
		
		try {
			ViewUriUtil.setURI(jCas, new URI(tokens[0]));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		// Clean text from double quotes		
		String text = tokens[1].replaceAll("^\"", "").replaceAll("\"$", "").replaceAll("\"\"", "\"") + " " + tokens[2].replaceAll("^\"", "").replaceAll("\"$", "").replaceAll("\"\"", "\"");

		jCas.setDocumentText(text);
		
		Section title = new Section(jCas);
		title.setSectionType("title");
		title.setBegin(0);
		title.setEnd(tokens[1].length());
		title.addToIndexes(jCas);
		
		Section abstractText = new Section(jCas);
		abstractText.setSectionType("abstract");
		abstractText.setBegin(tokens[1].length() + 1);
		abstractText.setEnd(tokens[1].length() + 1 + tokens[2].length());
		abstractText.addToIndexes(jCas);	
	}

	public static CollectionReaderDescription getDescriptionFromFiles(String fileName)
			throws ResourceInitializationException {
		return CollectionReaderFactory.createReaderDescription(LitCoinReader.class, null, PARAM_FILE_NAME, fileName);
	}
}