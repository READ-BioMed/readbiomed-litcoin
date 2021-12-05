package readbiomed.litcoin.nlp;

import java.io.IOException;

import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.xml.sax.SAXException;

import readbiomed.litcoin.consumer.JCas2LitCoin;
import readbiomed.litcoin.nlp.entities.NCBITaxonomyAnnotator;
import readbiomed.nlp.dictionary.ConceptMapperFactory;

public class AnnotationPipelineFactory {
	public static AggregateBuilder getPipeline(String dictFileName, String outputFolderName)
			throws InvalidXMLException, ResourceInitializationException, IOException, SAXException {
		AggregateBuilder builder = new AggregateBuilder();

		
		// Entity annotation
		builder.add(ConceptMapperFactory.create(dictFileName));

		builder.add(NCBITaxonomyAnnotator.getDescription());
		
		// Relation annotation

		// Consumer to generate LitCoin output
		builder.add(JCas2LitCoin.getOutputFolderDescription(outputFolderName));

		return builder;
	}
}