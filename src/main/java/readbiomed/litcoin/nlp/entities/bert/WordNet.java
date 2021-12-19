package readbiomed.litcoin.nlp.entities.bert;

import java.util.List;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;

public class WordNet {

	private Dictionary dictionary = null;

	public WordNet() throws JWNLException {
		dictionary = Dictionary.getDefaultResourceInstance();
	}

	public String getWordNetRandomSynonym(String word) throws JWNLException {
		IndexWord wword = dictionary.getIndexWord(POS.NOUN, word);

		List<Synset> senses = wword.getSenses();

		if (senses.size() > 0) {
			List<Word> words = senses.get(0).getWords();

			if (words.size() > 0) {
				return words.get((int) (Math.random() * (words.size() - 1))).getLemma();
			}
		}

		return word;
	}

	public static void main(String[] argc) throws JWNLException {
		WordNet w = new WordNet();

		System.out.println(w.getWordNetRandomSynonym("dog"));
	}

}