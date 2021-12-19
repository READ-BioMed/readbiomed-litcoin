package readbiomed.litcoin.nlp.entities.bert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class QueryPubTator {

	public static void main(String[] argc) throws FileNotFoundException, IOException {
		Set<String> pmids = new HashSet<String>();

		try (BufferedReader b = new BufferedReader(
				new FileReader("/Users/ajimeno/Documents/UoM/LitCoin/training/pubtator-pmids-test.txt"))) {
			for (String line; (line = b.readLine()) != null;) {
				if (line.trim().length() > 0) {
					pmids.add(line.trim());
				}
			}
		}

		StringBuilder lpmids = new StringBuilder();
		int count = 0;

		try (BufferedWriter w = new BufferedWriter(
				new FileWriter("/Users/ajimeno/Documents/UoM/LitCoin/training/pubtator-test.csv"))) {
			for (String pmid : pmids) {
				lpmids.append(",").append(pmid);
				count++;

				if (count == 100) {
					// Call Pubtator
					String queryURL = "https://www.ncbi.nlm.nih.gov/research/pubtator-api/publications/export/pubtator?pmids="
							+ lpmids.toString().substring(1);
					try (BufferedReader b = new BufferedReader(new InputStreamReader(new URL(queryURL).openStream()))) {
						for (String line; (line = b.readLine()) != null;) {
							w.write(line);
							w.newLine();
						}
					}

					// Send output to file

					count = 0;
					lpmids.setLength(0);
				}
			}

			if (count > 0) {
				String queryURL = "https://www.ncbi.nlm.nih.gov/research/pubtator-api/publications/export/pubtator?pmids="
						+ lpmids.toString().substring(1);
				try (BufferedReader b = new BufferedReader(new InputStreamReader(new URL(queryURL).openStream()))) {
					for (String line; (line = b.readLine()) != null;) {
						w.write(line);
						w.newLine();
					}
				}
			}
		}

		// If any document available, print
	}
}