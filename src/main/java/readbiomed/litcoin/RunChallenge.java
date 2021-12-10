package readbiomed.litcoin;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.xml.sax.SAXException;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import readbiomed.litcoin.nlp.AnnotationPipelineFactory;
import readbiomed.litcoin.reader.LitCoinReader;

@Command(name = "RunChallenge", mixinStandardHelpOptions = true, version = "RunChallenge 0.1", description = "Run LitCoin challenge pipeline.")
public class RunChallenge implements Callable<Integer> {

	@Parameters(index = "0", description = "Dictionary file name.")
	private String dictFileName;
	@Parameters(index = "1", description = "Input file name.")
	private String inputFileName;
	@Parameters(index = "2", description = "Output folder name.")
	private String outputFolderName;

	public static void main(String[] argc) throws IOException, SAXException, UIMAException {
		int exitCode = new CommandLine(new RunChallenge()).execute(argc);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		/*
		 * String text = "This is a H1N1 pathogen and human mutation rs6232.";
		 * 
		 * JCas jCas = JCasFactory.createJCas(); jCas.setDocumentText(text);
		 * 
		 * AggregateBuilder pipeline =
		 * AnnotationPipelineFactory.getPipeline(dictFileName, outputFolderName);
		 * AnalysisEngine ae =
		 * AnalysisEngineFactory.createEngine(pipeline.createAggregateDescription());
		 * 
		 * ae.process(jCas);
		 */

		CollectionReaderDescription litCoinReader = LitCoinReader.getDescriptionFromFiles(inputFileName);
		AggregateBuilder pipeline = AnnotationPipelineFactory.getPipeline(dictFileName, outputFolderName);

		SimplePipeline.runPipeline(litCoinReader, pipeline.createAggregateDescription());

		return 0;
	}
}