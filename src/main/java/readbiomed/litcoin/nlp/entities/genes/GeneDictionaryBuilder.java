package readbiomed.litcoin.nlp.entities.genes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringEscapeUtils;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import readbiomed.litcoin.nlp.entities.utils.Utils;

@Command(name = "BuildDictionary", mixinStandardHelpOptions = true, version = "BuildDictionary 0.1", description = "Run LitCoin challenge BuildDictionary for genes.")
public class GeneDictionaryBuilder implements Callable<Integer> {
	private static final Pattern p_tab = Pattern.compile("\\t");
	private static final Pattern p_pipe = Pattern.compile("\\|");

	private static void addTerm(String term, Set<String> terms, Set<String> stopwords) {
		term = term.replaceAll("'", "").toLowerCase();
		if (!stopwords.contains(term)) {
			terms.add(term);
		}
	}

	@Parameters(index = "0", description = "Input file name.")
	private String inputFileName;
	@Parameters(index = "1", description = "Output file name.")
	private String outputFileName;

	public static void main(String[] argc) throws IOException {
		int exitCode = new CommandLine(new GeneDictionaryBuilder()).execute(argc);
		System.exit(exitCode);
	}

	private boolean checkNumber(String term) {
		try {
			Double.parseDouble(term);

		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private boolean checkTerm(String term) {
		if (term.contains("[") || term.contains("]")) {
			return false;
		}

		term = term.replaceAll("'", "");

		// Remove short terms
		if (term.length() < 3) {
			return false;
		}

		// Remove numbers
		if (checkNumber(term)) {
			return false;
		}

		if (term.charAt(0) >= '0' && term.charAt(0) <= '9') {
			return false;
		}

		return true;
	}

	@Override
	public Integer call() throws Exception {
		Set<String> stopwords = Utils.getStopwords("readbiomed/litcoin/nlp/entities/genes/stopwords.txt");

		// 0 #tax_id
		// 1 GeneID
		// 2 Symbol
		// 3 LocusTag
		// 4 Synonyms
		// 5 dbXrefs
		// 6 chromosome
		// 7 map_location
		// 8 description
		// 9 type_of_gene
		// 10 Symbol_from_nomenclature_authority
		// 11 Full_name_from_nomenclature_authority
		// 12 Nomenclature_status
		// 13 Other_designations
		// 14 Modification_date
		// 15 Feature_type

		try (BufferedReader b = new BufferedReader(
				new InputStreamReader(new GZIPInputStream(new FileInputStream(inputFileName))))) {
			try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFileName))) {
				String line;

				w.write("<?xml version='1.0' encoding='UTF8'?>");
				w.newLine();
				w.write("<synonym>");
				w.newLine();

				// Remove header
				b.readLine();

				while ((line = b.readLine()) != null) {
					String[] token = p_tab.split(line);

					if (token.length == 16) {
						if (!token[2].equals("NEWENTRY") && !token[8].equals("hypothetical protein")) {
							Set<String> set = new HashSet<String>();

							// token[2] is a symbol, probably not helpful
							// addTerm(token[2], set, stopwords);

							for (String synonym : p_pipe.split(token[4])) {
								if (checkTerm(synonym))
									addTerm(synonym, set, stopwords);
							}

							if (checkTerm(token[10])) {
								addTerm(token[10], set, stopwords);
							}

							if (checkTerm(token[11])) {
								addTerm(token[11], set, stopwords);
							}

							if (set.size() > 0) {
								w.write("<token id=\"gene-" + StringEscapeUtils.escapeXml(token[1]) + "\">");
								w.newLine();

								for (String term : set) {
									w.write("<variant base=\"" + StringEscapeUtils.escapeXml(term) + "\"/>");
									w.newLine();
								}

								w.write("</token>");
								w.newLine();
							}
						}
					}
				}

				w.write("</synonym>");
				w.flush();
			}
		}

		return 0;
	}
}