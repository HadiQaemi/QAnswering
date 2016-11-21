package qclassifier;

import java.util.List;
import Wordnet.Wordnet;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.TaggedWord;

public class Stemmer 
{
	
	public static String getRoot(TaggedWord taggedWord)
	{
		POS pos = null;
		if (taggedWord.tag().startsWith("NN"))
			pos = POS.NOUN;
		else if (taggedWord.tag().startsWith("JJ"))
			pos = POS.ADJECTIVE;
		else if (taggedWord.tag().startsWith("RB"))
			pos = POS.ADVERB;
		else if (taggedWord.tag().startsWith("VB"))
			pos = POS.VERB;

		if (pos == null)
			return taggedWord.word();
		
		WordnetStemmer stemmer = Wordnet.getStemmer();
		Dictionary dict = Wordnet.getDictionary();
		
		/* in case the word is plural, search for the stem */
		List<String> stems = stemmer.findStems(taggedWord.word(), pos);
		if (!stems.isEmpty()) 
		{
			IIndexWord indexWord = dict.getIndexWord(stems.get(0), pos);
			if (indexWord != null)
				return indexWord.getLemma();
		}

		return taggedWord.word();

		/*
		if (_stemmer == null)
			_stemmer = new englishStemmer();
		
		_stemmer.setCurrent(word);
		_stemmer.stem();
		
		return _stemmer.getCurrent();
		*/
	}
}
