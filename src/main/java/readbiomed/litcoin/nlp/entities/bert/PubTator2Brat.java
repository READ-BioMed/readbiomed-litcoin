package readbiomed.litcoin.nlp.entities.bert;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.cas.admin.CASAdminException;
import org.apache.uima.fit.pipeline.SimplePipeline;

import readbiomed.litcoin.reader.PubTatorReader;

public class PubTator2Brat {
	public static void main(String[] argc) throws UIMAException, CASAdminException, IOException {
		String inputFileName = "/Users/ajimeno/Documents/UoM/LitCoin/training/pubtator-test.csv";
		String outputFolderName = "/Users/ajimeno/Documents/UoM/LitCoin/training/pubtator-test";

		SimplePipeline.runPipeline(PubTatorReader.getDescriptionFromFiles(inputFileName),
				JCas2Brat.getOutputFolderDescription(outputFolderName));
	}
}