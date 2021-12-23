package readbiomed.litcoin.nlp.entities.bert;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.token.tokenizer.TokenAnnotator;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import com.ibm.au.research.nlp.brat.BratGroundTruthReader;
import com.ibm.au.research.nlp.util.TextFileFilter;

import readbiomed.litcoin.nlp.entities.bert.post.AbbreviationIdentification;
import readbiomed.litcoin.nlp.entities.bert.post.PrefixIdentification;

public class Brat2LitCoin {

	public static void main(String[] argc) throws IOException, UIMAException {
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(UriToDocumentTextAnnotator.getDescription());
		builder.add(BratGroundTruthReader.getDescription());
		
		builder.add(SentenceAnnotator.getDescription());
		builder.add(TokenAnnotator.getDescription());
		//builder.add(PrefixIdentification.getDescription());
		builder.add(AbbreviationIdentification.getDescription());
		builder.add(FilterNestedEntities.getDescription());

		builder.add(FilterBioBERT.getDescription());
		builder.add(JCas2LitCoin.getOutputFolderDescription("/Users/ajimeno/Documents/UoM/LitCoin/testing/submission"));
		
		builder.add(JCas2Brat.getOutputFolderDescription("/Users/ajimeno/Documents/UoM/LitCoin/testing/submission/submission-001"));
//		builder.add(JCas2LitCoin.getOutputFolderDescription("/Users/ajimeno/Documents/UoM/LitCoin/training"));

		SimplePipeline.runPipeline(
				UriCollectionReader.getDescriptionFromFiles(FileUtils.listFiles(
						
						//new File("/Users/ajimeno/Documents/UoM/LitCoin/testing/entities-large"), new TextFileFilter(), null)),
				new File("/Users/ajimeno/Documents/git/brat/data/entities-large-targeted-merging"), new TextFileFilter(), null)),
				// new File("/Users/ajimeno/Documents/UoM/LitCoin/training/entities"), new
				// TextFileFilter(), null)),
				builder.createAggregateDescription());
	}
}