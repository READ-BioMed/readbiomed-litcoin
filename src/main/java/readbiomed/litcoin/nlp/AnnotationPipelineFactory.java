package readbiomed.litcoin.nlp;

import java.io.IOException;

import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.xml.sax.SAXException;

import com.ibm.au.research.nlp.ner.mutation.nlp.ner.WDDMutationRegExAnnotator;

import readbiomed.litcoin.consumer.JCas2LitCoin;
import readbiomed.litcoin.nlp.entities.celllines.CellLineAnnotator;
import readbiomed.litcoin.nlp.entities.chemicals.ChemicalAnnotator;
import readbiomed.litcoin.nlp.entities.diseases.DiseaseAnnotator;
import readbiomed.litcoin.nlp.entities.genes.GeneAnnotator;
import readbiomed.litcoin.nlp.entities.mutations.MutationFiltering;
import readbiomed.litcoin.nlp.entities.organisms.NCBITaxonomyAnnotator;
import readbiomed.litcoin.nlp.entities.utils.FilterRepeatedEntity;
import readbiomed.nlp.dictionary.ConceptMapperFactory;

public class AnnotationPipelineFactory {
	public static AggregateBuilder getPipeline(String dictFileName, String outputFolderName)
			throws InvalidXMLException, ResourceInitializationException, IOException, SAXException {
		AggregateBuilder builder = new AggregateBuilder();

		
		// Entity annotation
		builder.add(ConceptMapperFactory.create(dictFileName));

		builder.add(NCBITaxonomyAnnotator.getDescription());

		builder.add(GeneAnnotator.getDescription());

		builder.add(DiseaseAnnotator.getDescription());

		builder.add(CellLineAnnotator.getDescription());

		builder.add(ChemicalAnnotator.getDescription());

		builder.add(WDDMutationRegExAnnotator.getDescription());

		builder.add(MutationFiltering.getDescription());

        builder.add(FilterRepeatedEntity.getDescription());

		// Consumer to generate LitCoin output
		builder.add(JCas2LitCoin.getOutputFolderDescription(outputFolderName));

		return builder;
	}
}