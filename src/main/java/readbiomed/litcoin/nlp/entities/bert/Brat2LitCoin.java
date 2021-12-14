package readbiomed.litcoin.nlp.entities.bert;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import com.ibm.au.research.nlp.brat.BratGroundTruthReader;
import com.ibm.au.research.nlp.util.TextFileFilter;

public class Brat2LitCoin {

	public static void main(String[] argc) throws IOException, UIMAException {
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(UriToDocumentTextAnnotator.getDescription());
		builder.add(BratGroundTruthReader.getDescription());
		builder.add(FilterBioBERT.getDescription());
		builder.add(JCas2LitCoin.getOutputFolderDescription("/Users/ajimeno/Documents/UoM/LitCoin/testing/submission"));

		SimplePipeline.runPipeline(
				UriCollectionReader.getDescriptionFromFiles(FileUtils.listFiles(
						new File("/Users/ajimeno/Documents/UoM/LitCoin/testing/entities"), new TextFileFilter(), null)),
				builder.createAggregateDescription());
	}
}