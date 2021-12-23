package readbiomed.litcoin.nlp.entities.bert.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import au.com.nicta.csp.brateval.Annotations;
import au.com.nicta.csp.brateval.Document;
import au.com.nicta.csp.brateval.Entity;

/* 
 * Data augmentation by changing words within entities
 */
public class DataAugmentation {

	public static void main(String[] argc) throws FileNotFoundException, IOException {
		// Load PMIDs
		Set<String> pmids = new HashSet<>();

		try (BufferedReader b = new BufferedReader(
				new FileReader("/Users/ajimeno/Documents/UoM/LitCoin/training/pmids-augmentation.txt"))) {
			for (String line; (line = b.readLine()) != null;) {
				if (line.trim().length() > 0) {
					pmids.add(line.trim());
				}
			}
		}

		Map<String, List<String>> terms = new HashMap<>();

		Pattern p = Pattern.compile("\\|");

		// Load terms
		try (BufferedReader b = new BufferedReader(
				new FileReader("/Users/ajimeno/Documents/UoM/LitCoin/training/augmentation-terms-pubtator.txt"))) {
			for (String line; (line = b.readLine()) != null;) {
				if (line.trim().length() > 0) {
					String[] tokens = p.split(line);
					if (tokens.length == 2) {
						List<String> termSet = terms.get(tokens[0]);
						if (termSet == null) {
							termSet = new ArrayList<>();
							terms.put(tokens[0], termSet);
						}

						if (!termSet.contains(tokens[1])) {
							termSet.add(tokens[1]);
						}
					}
				}
			}
		}

		// Pick up pmids randomly
		List<String> ids = new ArrayList<>(pmids);

		for (int i = 1; i < 800; i++) {
			int random = (int) (Math.random() * (ids.size() - 1));
			String document = ids.get(random);
			System.out.println(i + "|" + document);

			// Read document
			String txtPath = "/Users/ajimeno/Documents/UoM/LitCoin/training/entities/" + document + ".txt";
			String annPath = "/Users/ajimeno/Documents/UoM/LitCoin/training/entities/" + document + ".ann";

			String text = new String(Files.readAllBytes(Paths.get(txtPath)), StandardCharsets.UTF_8);

			Document d = Annotations.read(annPath);

			Set<Entity> removeEntity = new HashSet<>();
			Set<Entity> addEntity = new HashSet<>();

			// Change 20% of the entities
			for (Entity e : d.getEntities()) {
				if (Math.random() <= .8) {
					List<String> termSet = terms.get(e.getType());

					String newTerm = termSet.get((int) (Math.random() * (termSet.size() - 1)));

					System.out.println("Document: " + document);
					System.out.println("New term: " + newTerm);
					System.out.println("Term: "
							+ text.substring(e.getLocations().get(0).getStart(), e.getLocations().get(0).getEnd()));
					System.out.println(e.getLocations().get(0).getStart());
					System.out.println(e.getLocations().get(0).getEnd());

					System.out.println("Before:" + text);

					// Let's assume just one location
					text = text.substring(0, e.getLocations().get(0).getStart()) + newTerm
							+ text.substring(e.getLocations().get(0).getEnd());

					System.out.println("After:" + text);

					int offset = newTerm.length()
							- (e.getLocations().get(0).getEnd() - e.getLocations().get(0).getStart());

					for (Entity e2 : d.getEntities()) {
						if (e.getLocations().get(0).getEnd() < e2.getLocations().get(0).getStart()) {
							e2.getLocations().get(0).setStart(e2.getLocations().get(0).getStart() + offset);
							e2.getLocations().get(0).setEnd(e2.getLocations().get(0).getEnd() + offset);
						}
					}

					e.getLocations().get(0).setEnd(e.getLocations().get(0).getEnd() + offset);

					Entity ne = new Entity(e.getId(), e.getType(), e.getLocations(),
							text.substring(e.getLocations().get(0).getStart(), e.getLocations().get(0).getEnd()),
							e.getFile());

					removeEntity.add(e);
					addEntity.add(ne);

					System.out.println(
							text.substring(e.getLocations().get(0).getStart(), e.getLocations().get(0).getEnd()));
					System.out.println(e.getLocations().get(0).getStart());
					System.out.println(e.getLocations().get(0).getEnd());
				}
			}

			removeEntity.stream().forEach(e -> d.getEntities().remove(e));
			addEntity.stream().forEach(e -> d.addEntity(e.getId(), e));

			int idDoc = (int) (Math.random() * 100000);

			Annotations.write("/Users/ajimeno/Documents/UoM/LitCoin/training/augmentation/" + document + idDoc + ".ann",
					d);

			Files.writeString(
					Paths.get(
							"/Users/ajimeno/Documents/UoM/LitCoin/training/augmentation/" + document + idDoc + ".txt"),
					text, StandardCharsets.UTF_8);
		}
	}
}