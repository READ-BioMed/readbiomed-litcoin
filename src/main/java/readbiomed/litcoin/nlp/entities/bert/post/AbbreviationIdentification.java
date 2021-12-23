package readbiomed.litcoin.nlp.entities.bert.post;

import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ne.type.NamedEntityMention;
import org.cleartk.token.type.Token;

public class AbbreviationIdentification extends JCasAnnotator_ImplBase {

	// https://psb.stanford.edu/psb-online/proceedings/psb03/schwartz.pdf
	private static String findBestLongForm(String shortForm, String longForm) {
		int sIndex; // The index on the short form
		int lIndex; // The index on the long form
		char currChar; // The current character to match

		sIndex = shortForm.length() - 1; // Set sIndex at the end of the
		// short form
		lIndex = longForm.length() - 1; // Set lIndex at the end of the
		// long form
		for (; sIndex >= 0; sIndex--) { // Scan the short form starting
			// from end to start
			// Store the next character to match. Ignore case
			currChar = Character.toLowerCase(shortForm.charAt(sIndex));
			// ignore non alphanumeric characters
			if (!Character.isLetterOrDigit(currChar))
				continue;
			// Decrease lIndex while current character in the long form
			// does not match the current character in the short form.
			// If the current character is the first character in the
			// short form, decrement lIndex until a matching character
			// is found at the beginning of a word in the long form.
			while (((lIndex >= 0) && (Character.toLowerCase(longForm.charAt(lIndex)) != currChar))
					|| ((sIndex == 0) && (lIndex > 0) && (Character.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
				lIndex--;
			// If no match was found in the long form for the current
			// character, return null (no match).
			if (lIndex < 0)
				return null;
			// A match was found for the current character. Move to the
			// next character in the long form.
			lIndex--;
		}
		// Find the beginning of the first word (in case the first
		// character matches the beginning of a hyphenated word).
		lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
		// Return the best long form, the substring of the original
		// long form, starting from lIndex up to the end of the original
		// long form.
		return longForm.substring(lIndex);
	}

	/*
	 * public static void main(String[] argc) throws UIMAException {
	 * 
	 * String sentence = "The abbreviation of obese (OB).";
	 * 
	 * JCas jCas = JCasFactory.createJCas(); jCas.setDocumentText(sentence);
	 * 
	 * AggregateBuilder builder = new AggregateBuilder();
	 * 
	 * builder.add(SentenceAnnotator.getDescription());
	 * builder.add(TokenAnnotator.getDescription());
	 * 
	 * AnalysisEngine ae =
	 * AnalysisEngineFactory.createEngine(builder.createAggregateDescription());
	 * 
	 * ae.process(jCas); }
	 */

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Token token : JCasUtil.select(jCas, Token.class)) {
			if (token.getCoveredText().equals("(")) {
				List<Token> ts = JCasUtil.selectFollowing(Token.class, token, 2);

				// This is an acronym
				if (ts.size() == 2 && ts.get(1).getCoveredText().equals(")")) {

					// Select the preceding entity if it exists
					List<NamedEntityMention> nes = JCasUtil.selectPreceding(NamedEntityMention.class, token, 1);

					if (nes.size() > 0) {
						NamedEntityMention ent = nes.get(0);
						String abbreviation = ts.get(0).getCoveredText();

						if (abbreviation.matches("[a-zA-Z]+")) {
							String longForm = findBestLongForm(abbreviation, ent.getCoveredText());

							if (longForm != null) {
								// If found, annotate all mentions, correct the incorrect ones
								for (Token tokenIn : JCasUtil.select(jCas, Token.class)) {
									if (tokenIn.getCoveredText().equals(abbreviation)) {
										// Check for existing entity
										List<NamedEntityMention> lIn = JCasUtil.selectAt(jCas, NamedEntityMention.class,
												tokenIn.getBegin(), tokenIn.getEnd());

										if (lIn.size() > 0) {
											for (NamedEntityMention neIn : lIn) {
												neIn.setMentionType(ent.getMentionType());
											}
										} else {
											// Otherwise, create a new one
											NamedEntityMention ne = new NamedEntityMention(jCas, tokenIn.getBegin(),
													tokenIn.getEnd());
											ne.setMentionType(ent.getMentionType());
											ne.addToIndexes(jCas);
										}
									}
								}
							}
						}
					}
				}
			}
		}

	}

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(AbbreviationIdentification.class);
	}
}