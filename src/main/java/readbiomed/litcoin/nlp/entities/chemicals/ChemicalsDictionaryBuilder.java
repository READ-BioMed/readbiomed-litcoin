package readbiomed.litcoin.nlp.entities.chemicals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

public class ChemicalsDictionaryBuilder {

	private static boolean isChemical(Element e) {
		for (Element etnl : e.getElementsByTag("TreeNumberList")) {
			for (Element etn : etnl.getElementsByTag("TreeNumber")) {
				if (etn.text().startsWith("D")) {
					return true;

				}
			}
		}
		return false;
	}

	public static void main(String[] argc) throws FileNotFoundException, IOException, XMLStreamException {
		Document doc = Jsoup.parse(new GZIPInputStream(new FileInputStream("/Users/ajimeno/Downloads/desc2022.gz")),
				"UTF-8", "https://meshb-prev.nlm.nih.gov/search");

		Map<String, Set<String>> chemicalTerms = new HashMap<>();

		for (Element e : doc.getElementsByTag("DescriptorRecord")) {
			if (isChemical(e)) {
				// Get MeSH id
				String meshId = e.getElementsByTag("DescriptorUI").get(0).text();
				Set<String> terms = new HashSet<>();

				chemicalTerms.put(meshId, terms);

				// Get descriptor name
				terms.add(e.getElementsByTag("DescriptorName").get(0).getElementsByTag("String").get(0).text().toLowerCase());

				// Get Concept List names -- unsure that we need this
				for (Element ecl : e.getElementsByTag("ConceptList")) {
					for (Element ec : ecl.getElementsByTag("Concept")) {
						for (Element ecn : ec.getElementsByTag("ConceptName")) {
							terms.add(ecn.getElementsByTag("String").get(0).text().toLowerCase());
						}

						for (Element etl : ec.getElementsByTag("TermList")) {
							for (Element et : etl.getElementsByTag("Term")) {
								terms.add(et.getElementsByTag("String").get(0).text().toLowerCase());
							}
						}
					}
				}
			}
		}

		// "/Users/ajimeno/Documents/UoM/LitCoin/dictionaries/cellLineDict.xml"
		//chemicalTerms.entrySet().stream().forEach(e-> System.out.println(e.getKey() + "/" + e.getValue()));
		System.out.println(chemicalTerms.size());
		
		// Write dictionary
		try (FileWriter w = new FileWriter("/Users/ajimeno/Documents/UoM/LitCoin/dictionaries/chemicalDict.xml")) {
			XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
			XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(xMLOutputFactory.createXMLStreamWriter(w));

			xmlWriter.writeStartDocument();
			xmlWriter.writeStartElement("synonym");

			for (Map.Entry<String, Set<String>> entry : chemicalTerms.entrySet()) {
				String preferredTerm = entry.getValue().toArray(new String[0])[0];

				xmlWriter.writeStartElement("token");
				xmlWriter.writeAttribute("id", "chemical-" + entry.getKey());
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
	}
}