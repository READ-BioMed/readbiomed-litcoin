package readbiomed.litcoin.nlp.entities.bert;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import com.ibm.au.research.nlp.brat.BratGroundTruthReader;
import com.ibm.au.research.nlp.util.TextFileFilter;

import readbiomed.litcoin.reader.LitCoinReader;

public class BratMixing {
	private static void brat2LitCoin(String bratFolder, String outputFolder) throws UIMAException, IOException {
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(UriToDocumentTextAnnotator.getDescription());
		builder.add(BratGroundTruthReader.getDescription());

		builder.add(JCas2LitCoin.getOutputFolderDescription(outputFolder));

		SimplePipeline.runPipeline(
				UriCollectionReader
						.getDescriptionFromFiles(FileUtils.listFiles(new File(bratFolder), new TextFileFilter(), null)),
				builder.createAggregateDescription());
	}

	public static void main(String[] argc) throws UIMAException, IOException {
		// Translate annotations to LitCoin
		brat2LitCoin("/Users/ajimeno/Documents/git/brat/data/entities-large",
				"/Users/ajimeno/Documents/git/brat/data/entities-large");
		brat2LitCoin("/Users/ajimeno/Documents/git/brat/data/entities-large-targeted",
				"/Users/ajimeno/Documents/git/brat/data/entities-large-targeted");

		AggregateBuilder builder = new AggregateBuilder();
		builder.add(LitCoin2JCas.getInputFileDescription(
				"/Users/ajimeno/Documents/git/brat/data/entities-large/submission_readbiomed_entities.csv"));
		builder.add(LitCoin2JCas.getInputFileDescription(
				"/Users/ajimeno/Documents/git/brat/data/entities-large-targeted/submission_readbiomed_entities.csv"));

		builder.add(FilterNestedEntities.getDescription());
		builder.add(JCas2Brat
				.getOutputFolderDescription("/Users/ajimeno/Documents/git/brat/data/entities-large-targeted-merging"));

		AnalysisEngine ae = AnalysisEngineFactory.createEngine(builder.createAggregateDescription());

		JCasCollectionReader_ImplBase cr = (JCasCollectionReader_ImplBase) org.apache.uima.fit.factory.CollectionReaderFactory
				.createReader(
						LitCoinReader.getDescriptionFromFiles("/Users/ajimeno/Downloads/LitCoin Dataset/abstracts_test.csv"));

		JCas jCas = JCasFactory.createJCas();
		// For each document
		while (cr.hasNext()) {
			System.out.println(jCas.getDocumentText());
			cr.getNext(jCas);
			// Add the entities and save as brat
			ae.process(jCas);
			jCas.reset();
		}
	}
}