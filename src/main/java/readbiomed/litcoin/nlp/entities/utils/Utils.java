package readbiomed.litcoin.nlp.entities.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import readbiomed.litcoin.nlp.entities.genes.GeneDictionaryBuilder;

public class Utils {
	public static Set<String> getStopwords(String filePath) throws FileNotFoundException, IOException {
		Set<String> words = new HashSet<>();

		try (BufferedReader b = new BufferedReader(new InputStreamReader(GeneDictionaryBuilder.class.getClassLoader()
				.getResourceAsStream(filePath)))) {

			for (String line; (line = b.readLine()) != null;) {
				String word = line.trim();

				if (word.length() > 0) {
					words.add(word.toLowerCase());
				}
			}
		}
		return words;
	}

}