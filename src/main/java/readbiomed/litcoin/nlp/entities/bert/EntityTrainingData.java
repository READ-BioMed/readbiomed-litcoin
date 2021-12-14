package readbiomed.litcoin.nlp.entities.bert;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import readbiomed.litcoin.reader.LitCoinReader;

public class EntityTrainingData {

	public static void main(String[] argc) throws IOException, UIMAException {
		String inputFileName = "/Users/ajimeno/Downloads/LitCoin Dataset/abstracts_train.csv";

		AggregateBuilder builder = new AggregateBuilder();
		builder.add(Litcoin2JCas.getInputFileDescription("/Users/ajimeno/Downloads/LitCoin Dataset/entities_train.csv"));
		builder.add(JCas2Brat.getOutputFolderDescription("/Users/ajimeno/Documents/UoM/LitCoin/training/entities"));
		
		AnalysisEngine ae = AnalysisEngineFactory.createEngine(builder.createAggregateDescription());

		JCasCollectionReader_ImplBase cr = (JCasCollectionReader_ImplBase) org.apache.uima.fit.factory.CollectionReaderFactory
				.createReader(LitCoinReader.getDescriptionFromFiles(inputFileName));

		JCas jCas = JCasFactory.createJCas();
		// For each document
		while (cr.hasNext()) {
			cr.getNext(jCas);
			// Add the entities and save as brat
			ae.process(jCas);
			jCas.reset();
		}
	}
}