package readbiomed.litcoin.nlp.entities.pubtator;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringEscapeUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

@Command(name = "DictionaryBuilder", version = "DictionaryBuilder 0.1", description = "Build pathogen dictionary based on bioconcepts2pubtatorcentral.gz")
public class DictionaryBuilder implements Callable<Integer> {

	// maps index to a HashMap which maps variant names to their use frequency.
	private Map<String, ConcurrentHashMap<String, Integer>> entitiesMap = new HashMap<>();
	private List<String> stopWords = new ArrayList<>();
	private List<String> linesToRemove = new ArrayList<>();

	public void collectStopWords() throws IOException {
		System.out.println("Collecting stop words ... ");
		String stopWordsFilePath = "/Users/sonya/Documents/litcoin challenge/readbiomed-litcoin/resources/com/ibm/au/research/nlp/stopwords/stopwords-dictionary-builder.txt";
		BufferedReader br = new BufferedReader(new FileReader(stopWordsFilePath));

		String line;
		while ((line = br.readLine()) != null) {
			stopWords.add(line);
		}

		System.out.println("Finished collecting stop words.");
	}


	public void collectEntriesIntoMap(String pubtatorFileName) throws IOException {
		System.out.println("Collecting entries into map...");
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
				String names = splitLine[3].toLowerCase();
				if (names.length() != 0 && !id.contains("\\|")) {
					String[] namesArr = names.split("\\|");
					// if map already contains the entity
					if (entitiesMap.containsKey(id)) {
						ConcurrentHashMap<String, Integer> prevNames = entitiesMap.get(id);
						for (String name : namesArr) {
							if(!stopWords.contains(name)) {
								prevNames.put(name, 0);
							}
						}
					} else if (!id.equals("gene-None") && !id.equals("chemical--")) {
						ConcurrentHashMap<String, Integer> mapToAdd = new ConcurrentHashMap<>();
						for (String name : namesArr) {
							if(!stopWords.contains(name)) {
								mapToAdd.put(name, 0);
							}
						}
						entitiesMap.put(id, mapToAdd);
					}
				}
			}
		}
		System.out.println("Finished collecting entries into map.");
	}


	public void collectingFreqCount(String freqCountFile) throws IOException {
		System.out.println("Collecting freqCount into map...");
		BufferedReader br = new BufferedReader(new FileReader(freqCountFile));

		String line;
		while ((line = br.readLine()) != null) {
			if (line.toCharArray()[2] == ' ') {
				line = line.substring(3);
			} else if (line.toCharArray()[1] == ' ') {
				line = line.substring(2);
			} else if (line.toCharArray()[0] == ' ') {
				line = line.substring(1);
			}

			String[] splitLine = line.split(" ", 2);
			String count = splitLine[0];
			String[] nameAndId = splitLine[1].split("\\|", 2);
			String id = nameAndId[0];

			//fixing format of id to match enetityMap
			String entityType = id.split("-",2)[0].toLowerCase();

			try {
				id = entityType + "-" + id.split("-",2)[1];
			} catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
				id = entityType + "--";
			}

			String name = nameAndId[1].toLowerCase();

			try {
				entitiesMap.get(id).computeIfPresent(name, (k,v) -> v + Integer.valueOf(count));
			} catch (NullPointerException nullPointerException) {
				System.err.println("ERROR:" + id + " could not be found");
			}
		}
	}


	private void cleanUpDictionary() {
		System.out.println("Cleaning up dictionary...");
		entitiesMap.entrySet().forEach(entry -> {
			deleteSimilarEntries(entry.getValue().keySet(), entry.getValue());
		});
		System.out.println("Finished cleaning up dictionary.");
	}


	private void deleteSimilarEntries(Set<String> namesToCompare, ConcurrentHashMap<String, Integer> freqCount) {
		for (String nameToCompare : namesToCompare) {
			try {
				freqCount.entrySet().removeIf(entry -> (entry.getKey().contains(nameToCompare)
						&& freqCount.get(nameToCompare) > freqCount.get(entry.getKey())));
			} catch (NullPointerException nullPointerException){
				System.err.println("ERROR:" + nameToCompare + " could not be found");
			}
		}
	}


	private void writeDictionary(String outputFileName) throws IOException {
		System.out.println("Writing dictionary to XML...");
		try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFileName))) {

			w.write("<?xml version='1.0' encoding='UTF8'?>");
			w.newLine();
			w.write("<synonym>");
			w.newLine();

			entitiesMap.entrySet().forEach(entry -> {
				if(!entry.getValue().isEmpty()) {
					try {
						w.write("<token id=\"" + StringEscapeUtils.escapeXml(entry.getKey()) + "\">");
						w.newLine();

						entry.getValue().entrySet().forEach(nameEntry -> {
							try {
								w.write("<variant base=\"" + StringEscapeUtils.escapeXml(nameEntry.getKey()) + "\"/>");
								w.newLine();
							} catch (IOException e) {
								e.printStackTrace();
							}
						});
						w.write("</token>");
						w.newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			w.write("</synonym>");
			w.flush();
		}
		System.out.println("Finished writing dictionary to XML.");
	}


	// method to remove extra added stop words to reduce time running code.
	// To run this method, uncomment all other methods from "call()" function other than "collectStopWords"
	private void updateStopWords(String dictFileName, String newDictFileName) throws IOException {
		System.out.println("Updating Dictionary ...");
		BufferedReader br = new BufferedReader(new FileReader(dictFileName));

		String line;
		while ((line = br.readLine()) != null) {
			if(line.length() > 14) {
				for(String stopWord : stopWords) {
					if(line.substring(1,13).equals("variant base")
					&& line.substring(15, line.lastIndexOf(">") - 2).equals(stopWord)) {
						linesToRemove.add(line);
						System.out.println("Removed " + line);
					}
				}
			}
		}
		try (BufferedWriter w = new BufferedWriter(new FileWriter(newDictFileName))) {
			br = new BufferedReader(new FileReader(dictFileName));
			while ((line = br.readLine()) != null) {
				if(!linesToRemove.contains(line)) {
					w.write(line);
					w.newLine();
				}
			}
			w.flush();
		}
	}


	@Parameters(index = "0", description = "pubtator file name.", defaultValue = "/Users/sonya/Documents/litcoin challenge/bioconcepts2pubtatorcentral.gz")
	private String pubtatorFileName;

	@Parameters(index = "1", description = "Output dictionary file name.", defaultValue = "/Users/sonya/Documents/litcoin challenge/pubtatorDict.xml")
	private String outputDictionaryFileName;

	@Parameters(index = "2", description = "Frequency File Name.", defaultValue = "/Users/sonya/Documents/litcoin challenge/dict.csv")
	private String freqCountFileName;

	@Override
	public Integer call() throws Exception {
		collectStopWords();
		collectEntriesIntoMap(pubtatorFileName);
		collectingFreqCount(freqCountFileName);
		cleanUpDictionary();
//		updateStopWords(outputDictionaryFileName,"/Users/sonya/Documents/litcoin challenge/newPubtatorDict.xml");
		writeDictionary(outputDictionaryFileName);

		return 0;
	}

	public static void main(String... args) {
		int exitCode = new CommandLine(new DictionaryBuilder()).execute(args);
		System.exit(exitCode);
	}
}
