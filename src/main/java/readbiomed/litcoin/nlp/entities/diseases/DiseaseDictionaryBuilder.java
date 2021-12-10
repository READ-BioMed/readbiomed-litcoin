package readbiomed.litcoin.nlp.entities.diseases;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import readbiomed.litcoin.nlp.entities.utils.Utils;

@Command(name = "BuildDictionary", mixinStandardHelpOptions = true, version = "BuildDictionary 0.1", description = "Run LitCoin challenge BuildDictionary for diseases.")
public class DiseaseDictionaryBuilder implements Callable<Integer> {
	private static Pattern p = Pattern.compile("\\|");

	@Parameters(index = "0", description = "Input file name.")
	private String inputFileName;
	@Parameters(index = "1", description = "Output file name.")
	private String outputFileName;

	public static void main(String[] argc) throws FileNotFoundException, IOException, XMLStreamException {
		int exitCode = new CommandLine(new DiseaseDictionaryBuilder()).execute(argc);
		System.exit(exitCode);
	}

	// 0 #CUI
	// 1 TS
	// 2 STT
	// 3 ISPREF
	// 4 AUI
	// 5 SAUI
	// 6 SCUI
	// 7 SDUI
	// 8 SAB
	// 9 TTY
	// 10 CODE
	// 11 STR
	// 12 SUPPRESS

	@Override
	public Integer call() throws Exception {
		Set<String> stopwords = Utils.getStopwords("readbiomed/litcoin/nlp/entities/diseases/stopwords.txt");
		
		Map<String, Set<String>> cuiTerms = new HashMap<>();
		Map<String, String> cuiPreferredTerm = new HashMap<>();

		// Read file
		try (BufferedReader b = new BufferedReader(
				new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFileName))))) {
			// Remove header
			b.readLine();

			for (String line; (line = b.readLine()) != null;) {
				String[] tokens = p.split(line);

				// Is it a valid term
				if (tokens[12].equals("N")) {
					String term = tokens[11].toLowerCase();

					if (term.length() > 2 && !stopwords.contains(term)) {

						cuiTerms.computeIfAbsent(tokens[0], (k) -> new HashSet<String>()).add(term);

						if (tokens[9].equals("PT")) {
							cuiPreferredTerm.put(tokens[0], term);
						}
					}
				}
			}
		}

		System.out.println(cuiTerms.size());
		System.out.println(cuiPreferredTerm.size());

		// Write dictionary
		try (FileWriter w = new FileWriter(outputFileName)) {
			XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(xMLOutputFactory.createXMLStreamWriter(w));

			xmlWriter.writeStartDocument();
			xmlWriter.writeStartElement("synonym");

			for (Map.Entry<String, Set<String>> entry : cuiTerms.entrySet()) {
				String preferredTerm = (cuiPreferredTerm.get(entry.getKey()) != null
						? cuiPreferredTerm.get(entry.getKey())
						: entry.getValue().toArray(new String[0])[0]);

				xmlWriter.writeStartElement("token");
				xmlWriter.writeAttribute("id", "disease-" + entry.getKey());
				xmlWriter.writeAttribute("canonical", preferredTerm);

				for (String term : entry.getValue()) {
					xmlWriter.writeStartElement("variant");
					xmlWriter.writeAttribute("base", term);
					xmlWriter.writeEndElement();

				}

				xmlWriter.writeEndElement();
			}

			// End Synonym
			xmlWriter.writeEndElement();
			xmlWriter.writeEndDocument();
			xmlWriter.flush();
			xmlWriter.close();
		}
		return 0;
	}
}