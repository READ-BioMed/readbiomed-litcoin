package readbiomed.litcoin.nlp.entities.bert.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.admin.CASAdminException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.opennlp.tools.SentenceAnnotator;
import org.cleartk.token.type.Sentence;
import org.cleartk.util.ViewUriUtil;

import readbiomed.litcoin.nlp.entities.bert.LitCoin2JCas;
import readbiomed.litcoin.reader.LitCoinReader;

public class EntityFilteringTrainingData {

	public static void main(String[] argc) throws UIMAException, CASAdminException, IOException {
		String inputFileName = "/Users/ajimeno/Downloads/LitCoin Dataset/abstracts_train.csv";
		String outputFileName = "/Users/ajimeno/Documents/UoM/LitCoin/output/filterTrain.pipe";

		AnalysisEngine ae_gt = AnalysisEngineFactory.createEngine(
				LitCoin2JCas.getInputFileDescription("/Users/ajimeno/Downloads/LitCoin Dataset/entities_train.csv"));

		AggregateBuilder builder = new AggregateBuilder();
		builder.add(SentenceAnnotator.getDescription());
		builder.add(LitCoin2JCas.getInputFileDescription(
				"/Users/ajimeno/Documents/UoM/LitCoin/output/submission_readbiomed_entities.csv"));

		AnalysisEngine ae_ann = AnalysisEngineFactory.createEngine(builder.createAggregateDescription());

		JCasCollectionReader_ImplBase cr = (JCasCollectionReader_ImplBase) org.apache.uima.fit.factory.CollectionReaderFactory
				.createReader(LitCoinReader.getDescriptionFromFiles(inputFileName));

		try (BufferedWriter w = new BufferedWriter(new FileWriter(outputFileName))) {
			w.write("PMID|Text|Category");
			w.newLine();

			JCas jCas_gt = JCasFactory.createJCas();
			JCas jCas_ann = JCasFactory.createJCas();
			// For each document
			while (cr.hasNext()) {
				cr.getNext(jCas_gt);
				// Add the entities and save as brat
				ae_gt.process(jCas_gt);

				jCas_ann.setDocumentText(jCas_gt.getDocumentText());
				ViewUriUtil.setURI(jCas_ann, ViewUriUtil.getURI(jCas_gt));
				ae_ann.process(jCas_ann);

				for (NamedEntityMention ne : JCasUtil.select(jCas_ann, NamedEntityMention.class)) {

					for (Sentence s : JCasUtil.selectCovering(jCas_ann, Sentence.class, ne)) {
						String text = s.getCoveredText();

						int begin = ne.getBegin() - s.getBegin();
						int end = ne.getEnd() - s.getBegin();

						text = text.substring(0, end) + "##" + text.substring(end);
						text = text.substring(0, begin) + "@@" + ne.getMentionType() + "$$" + text.substring(begin);

						boolean isCorrect = false;
						for (NamedEntityMention ne_gt : JCasUtil.selectAt(jCas_gt, NamedEntityMention.class,
								ne.getBegin(), ne.getEnd())) {
							if (ne_gt.getMentionType().equals(ne.getMentionType())) {
								isCorrect = true;
								break;
							}
						}

						w.write(ViewUriUtil.getURI(jCas_gt) + "|" + text + "|" + (isCorrect ? "Y" : "N"));
						w.newLine();
					}
				}

				jCas_gt.reset();
				jCas_ann.reset();
			}
		}
	}
}