package readbiomed.litcoin.nlp.entities.celllines;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import readbiomed.litcoin.nlp.entities.utils.Utils;

@Command(name = "BuildDictionary", mixinStandardHelpOptions = true, version = "BuildDictionary 0.1", description = "Run LitCoin challenge BuildDictionary for cell lines.")
public class CellLineDictionaryBuilder implements Callable<Integer> {
	@Parameters(index = "0", description = "Input file name.")
	private String inputFileName;
	@Parameters(index = "1", description = "Output file name.")
	private String outputFileName;

	public static void main(String[] argc) throws FileNotFoundException, IOException, XMLStreamException {
		int exitCode = new CommandLine(new CellLineDictionaryBuilder()).execute(argc);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		Set<String> stopwords = Utils.getStopwords("readbiomed/litcoin/nlp/entities/celllines/stopwords.txt");
		
		Map<String, Set<String>> cellLineTerms = new HashMap<>();

		try (BufferedReader b = new BufferedReader(new FileReader(inputFileName))) {
			String id = null;

			for (String line; (line = b.readLine()) != null;) {
				line = line.trim();

				if (line.equals("[TERM]")) {
					id = null;
				} else if (line.startsWith("id:")) {
					id = line.substring(4);
				} else if (line.startsWith("synonym:")) {
					if (id != null) {
						String term = line.substring(10, line.indexOf("\" RELATED []")).trim().toLowerCase();

						if (term.length() > 2 && !stopwords.contains(term)) {
							cellLineTerms.computeIfAbsent(id, (k) -> new HashSet<String>()).add(term);
						}
					}
				}
			}
		}

		// Write dictionary
		try (FileWriter w = new FileWriter(outputFileName)) {
			XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(xMLOutputFactory.createXMLStreamWriter(w));

			xmlWriter.writeStartDocument();
			xmlWriter.writeStartElement("synonym");

			for (Map.Entry<String, Set<String>> entry : cellLineTerms.entrySet()) {
				String preferredTerm = entry.getValue().toArray(new String[0])[0];

				xmlWriter.writeStartElement("token");
				xmlWriter.writeAttribute("id", "cellline-" + entry.getKey());
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