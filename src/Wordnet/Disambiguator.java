package Wordnet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.stanford.nlp.ling.TaggedWord;

public class Disambiguator 
{
	private static Dictionary _dict;
	private static WordnetStemmer _stemmer;
	private List<TaggedWord> _sentence;
	private List<IIndexWord> _indexedSentence;
	
	public Disambiguator(List<TaggedWord> sentence)
	{
		_sentence = sentence;
		
		if (_dict == null)
		{
			_dict = Wordnet.getDictionary();
			_stemmer = Wordnet.getStemmer();
		}
		
		makeIndexedSentence();
	}
		
	private void makeIndexedSentence()
	{
		_indexedSentence = new ArrayList<IIndexWord>();
		
		for (TaggedWord tword : _sentence)
		{
			IIndexWord iword = taggedToIndexWord(tword);
			if (iword != null)
				_indexedSentence.add(iword);
		}
	}
	
	public List<IWord> getDisambiguatedSentence()
	{
		List<IWord> disambgSentence = new ArrayList<IWord>();
		for (TaggedWord tword : _sentence)
		{
			IWord dword = disambiguateSense(tword);
			if (dword != null)
				disambgSentence.add(dword);
		}
		
		return disambgSentence;
	}
	
	public IWord disambiguateSense(TaggedWord taggedWord) 
	{
		IIndexWord indexedWord = taggedToIndexWord(taggedWord);
		
		if (indexedWord == null)
			return null;
		
		int maxCount = -1;
		IWord optimum = null;
		
		/* for each sense */
		for (IWordID wordId : indexedWord.getWordIDs()) 
		{
			int count = 0;
			
			IWord word = _dict.getWord(wordId);
			/* TODO: not iterate only on indexed senses */
			for (IIndexWord contextIndexWord : _indexedSentence) 
			{
				/* subMax = maximum number of common words in s 
				 * definition (gloss) and definition of any sense of w */
				int subMax = 0;
				
				for (IWordID contextWordId : contextIndexWord.getWordIDs()) 
				{
					IWord contextWord = _dict.getWord(contextWordId);
					int sub = countCommonWords(word.getSynset().getGloss(), contextWord.getSynset().getGloss());
					if (sub > subMax) 
						subMax = sub;
				}
				count += subMax;
			}
			
			if (count > maxCount) {
				maxCount = count;
				optimum = word;
			}
		}
		
		return optimum;
	}

	private int countCommonWords(String gloss1, String gloss2) 
	{
		HashSet<String> wordsA = new HashSet<String>(Arrays.asList(gloss1.toLowerCase().split(" ")));
		HashSet<String> wordsB = new HashSet<String>(Arrays.asList(gloss2.toLowerCase().split(" ")));
		wordsA.retainAll(wordsB);
		
		return wordsA.size();
	}
	
	private IIndexWord taggedToIndexWord(TaggedWord taggedWord) 
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
		else 
			return null;

		
		/* in case the word is plural, search for the stem */
		List<String> stems = _stemmer.findStems(taggedWord.word(), pos);
		if (!stems.isEmpty()) 
		{
			IIndexWord indexWord = _dict.getIndexWord(stems.get(0), pos);
			return indexWord;
		}
		else 
			return null;
	}

}
