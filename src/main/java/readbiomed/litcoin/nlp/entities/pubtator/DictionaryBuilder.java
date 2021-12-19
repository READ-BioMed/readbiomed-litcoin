package readbiomed.litcoin.nlp.entities.pubtator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringEscapeUtils;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "DictionaryBuilder", version = "DictionaryBuilder 0.1", description = "Build pathogen dictionary based on bioconcepts2pubtatorcentral.txt.")
public class DictionaryBuilder implements Callable<Integer> {

	// maps index to name
	private Map<String, HashSet<String>> entitiesMap = new HashMap<>();

	public void collectEntriesIntoMap(String pubtatorFileName) throws IOException {
		// read document line by line (entry by entry)
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new GZIPInputStream(new FileInputStream(pubtatorFileName))))) {
			String line;
			while ((line = br.readLine()) != null) {
				// values are separated by tabs
				String[] splitLine = line.split("\t");

				// get id
				String entityType = splitLine[1];
				String index = splitLine[2];
				String id = entityType.toLowerCase() + "-" + index;

				// get names
				String names = splitLine[3];
				if (names != "") {
					String[] namesArr = names.split("\\|");
					HashSet<String> namesSet = new HashSet<>(Arrays.asList(namesArr));

					// if map already contains the entity
					if (entitiesMap.containsKey(id)) {
						HashSet<String> prevNames = entitiesMap.get(id);
						for (String name : namesSet) {
							prevNames.add(name);
						}
					} else {
						entitiesMap.put(id, namesSet);
					}
				}
			}
		}
	}

	private void writeDictionary(String outputFileName) throws IOException, XMLStreamException {
		try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFileName))) {

			w.write("<?xml version='1.0' encoding='UTF8'?>");
			w.newLine();
			w.write("<synonym>");
			w.newLine();

			entitiesMap.entrySet().forEach(entry -> {
				try {
					w.write("<token id=\"" + StringEscapeUtils.escapeXml(entry.getKey()) + "\">");
					w.newLine();

					for (String name : entry.getValue()) {
						if (name != null && name != "") {
							w.write("<variant base=\"" + StringEscapeUtils.escapeXml(name) + "\"/>");
							w.newLine();
						}
					}
					w.write("</token>");
					w.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			w.write("</synonym>");
			w.flush();
		}
	}

	@Parameters(index = "0", description = "pubtator file name.", defaultValue = "/Users/sonya/Documents/litcoin challenge/bioconcepts2pubtatorcentral.sample.txt")
	private String pubtatorFileName;

	@Parameters(index = "1", description = "Output dictionary file name.", defaultValue = "/Users/sonya/Documents/litcoin challenge/pubtatorDict.xml")
	private String outputDictionaryFileName;

	@Override
	public Integer call() throws Exception {
		collectEntriesIntoMap(pubtatorFileName);
		writeDictionary(outputDictionaryFileName);
		return 0;
	}

	public static void main(String... args) {
		int exitCode = new CommandLine(new DictionaryBuilder()).execute(args);
		System.exit(exitCode);
	}
}
