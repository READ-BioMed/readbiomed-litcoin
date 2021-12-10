package readbiomed.litcoin.nlp.entities.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "MergeDictionaries", mixinStandardHelpOptions = true, version = "MergeDictionaries 0.1", description = "Merge ConceptMapper dictionaries.")
public class MergeDictionaries implements Callable<Integer> {
	@Parameters(index = "0", description = "Input file name.")
	private String inputFileName;
	@Parameters(index = "1", description = "Output file name.")
	private String outputFileName;

	public static void main(String[] argc) throws IOException {
		int exitCode = new CommandLine(new MergeDictionaries()).execute(argc);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		// Load dictionary file
		Set<String> filePaths = new HashSet<>();

		try (BufferedReader b = new BufferedReader(new FileReader(inputFileName))) {
			for (String line; (line = b.readLine()) != null;) {
				line = line.trim();

				if (line.length() > 0) {
					filePaths.add(line);
				}
			}
		}

		if (filePaths.size() > 0) {
			try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFileName))) {
				w.write("<?xml version='1.0' encoding='UTF8'?>\n");
				w.write("<synonym>\n");

				// Save dictionaries in new file
				for (String filePath : filePaths) {
					boolean inDict = false;

					try (BufferedReader b = new BufferedReader(new FileReader(filePath))) {
						for (String line; (line = b.readLine()) != null;) {
							if (line.startsWith("<synonym>")) {
								inDict = true;
							} else if (line.startsWith("</synonym>")) {
								inDict = false;
							} else {
								if (inDict) {
									w.write(line);
									w.newLine();
								}
							}
						}
					}
				}

				w.write("</synonym>\n");
			}
		}

		return 0;
	}
}