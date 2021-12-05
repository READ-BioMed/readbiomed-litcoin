package readbiomed.litcoin;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.xml.sax.SAXException;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import readbiomed.litcoin.nlp.AnnotationPipelineFactory;

@Command(name = "RunChallenge", mixinStandardHelpOptions = true, version = "RunChallenge 0.1", description = "Run LitCoin challenge pipeline.")
public class RunChallenge implements Callable<Integer> {

	@Parameters(index = "0", description = "Dictionary file name.")
	private String dictFileName;
	@Parameters(index = "1", description = "Output folder name.")
	private String outputFolderName;

	
	public static void main(String[] argc)
			throws IOException, SAXException, UIMAException {
		int exitCode = new CommandLine(new RunChallenge()).execute(argc);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		String text = "This is a H1N1 pathogen.";
		
		JCas jCas = JCasFactory.createJCas();
		jCas.setDocumentText(text);
		
		AggregateBuilder pipeline = AnnotationPipelineFactory.getPipeline(dictFileName, outputFolderName);
		AnalysisEngine ae = AnalysisEngineFactory.createEngine(pipeline.createAggregateDescription());
		
		ae.process(jCas);
	
		return 0;
	}
}