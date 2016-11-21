package qclassifier.features;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import qclassifier.Question;
import qclassifier.features.QueryExpantion.Expansion;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.morph.WordnetStemmer;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.stanford.nlp.ling.TaggedWord;

/**
 * WordNet-hypernyms (suggested in 'Question Classification using HeadWords and their Hypernyms').
 */
public class Hypernyms extends FeatureBuilder 
{
	private IDictionary dict;
	private IStemmer stemmer;
	private Expansion _expansion;
	
	private static final int DEPTH = 4;
	
	public Hypernyms(double featureWeight, Expansion expantion) throws MalformedURLException 
	{
		_featureWeight = featureWeight;
		_expansion = expantion;
		dict = new Dictionary(new File("models/dict").toURI().toURL());
		dict.open();
		
		stemmer = new WordnetStemmer(dict);
	}
	
	@Override
	public void addFeatures(Question question) throws IOException, ClassNotFoundException 
	{
		HashSet<IWord> uniqueHypernyms;
		if (_expansion == Expansion.HeadWord)
			uniqueHypernyms = headWordHypernyms(question);
		else
			uniqueHypernyms = questionHypernyms(question);
		
		// add hypernyms as features
		for (IWord word : uniqueHypernyms) 
		{
			//question.addFeature("wordnet-hyp:" + word.getLemma(), _featureWeight);
			question.addFeature(word.getLemma(), _featureWeight);
		}
                


	}
	
	private HashSet<IWord> questionHypernyms(Question question) throws IOException, ClassNotFoundException
	{
		List<TaggedWord> questionTagged = question.getTaggedQuestion();

		HashSet<IWord> uniqueHypernyms = new HashSet<IWord>();
		
		for (TaggedWord tword : questionTagged) 
		{
			// look up head word
			IIndexWord indexedTword = taggedToIndexWord(tword);
			if (indexedTword == null) continue;
			
			// look up context words
			List<IIndexWord> contextWords = new ArrayList<IIndexWord>();
			for (TaggedWord word : questionTagged) 
			{
				IIndexWord indexWord = taggedToIndexWord(word);
				if (indexWord != null && !indexedTword.equals(indexWord)) {
					contextWords.add(indexWord);
				}
			}
			
			if (contextWords.isEmpty()) continue;
			
			// look up disambiguated hypernyms
			List<IWord> hypernyms = hypernyms(indexedTword, contextWords);
			uniqueHypernyms.addAll(hypernyms);			
		}
		
		return uniqueHypernyms;
	}
	
	private HashSet<IWord> headWordHypernyms(Question question) throws IOException, ClassNotFoundException
	{
		TaggedWord headWord = question.getHeadWord();
		HashSet<IWord> uniqueHypernyms = new HashSet<IWord>();
		
		if (headWord != null) 
		{
			// look up head word
			IIndexWord headIndexWord = taggedToIndexWord(headWord);
			if (headIndexWord == null) return uniqueHypernyms;
			
			// look up context words
			List<IIndexWord> contextWords = new ArrayList<IIndexWord>();
			for (TaggedWord word : question.getTaggedQuestion()) 
			{
				IIndexWord indexWord = taggedToIndexWord(word);
				if (indexWord != null && !headIndexWord.equals(indexWord)) {
					contextWords.add(indexWord);
				}
			}
			
			if (contextWords.isEmpty()) return uniqueHypernyms;
			
			// look up disambiguated hypernyms
			List<IWord> hypernyms = hypernyms(headIndexWord, contextWords);
			uniqueHypernyms.addAll(hypernyms);
		}
		
		return uniqueHypernyms;
	}
        // add hadi
	private void headWordSynset(Question question) throws IOException, ClassNotFoundException
	{
            
            
            //return uniqueSynset;//uniqueHypernyms;
	}
	private List<IWord> hypernyms(IIndexWord indexWord, List<IIndexWord> contextWords) {
		IWordID wordId = disambiguateSense(indexWord, contextWords);
		IWord word = dict.getWord(wordId);
		return hypernyms(0, word, contextWords);
	}
	
	private List<IWord> hypernyms(int level, IWord word, List<IIndexWord> contextWords) {
		ISynset synset = word.getSynset();
		
		List<IWord> result = new ArrayList<IWord>();
		result.add(word);
		
		// get the hypernyms
		List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
		
		for(ISynsetID sid : hypernyms){
			for (IWord hypernym : dict.getSynset(sid).getWords()) {
				if (level==DEPTH) {
					result.add(hypernym);
				} else {
					// recursively collect more hypernyms
					result.addAll(hypernyms(level+1, hypernym, contextWords));
				}
			}
		}

		return result;
	}

	//Implement Lesk's WSD algorithm
	private IWordID disambiguateSense(IIndexWord indexWord, List<IIndexWord> contextWords) {
		int maxCount = -1;
		IWordID optimum = null;
		
		// for each sense
		for (IWordID wordId : indexWord.getWordIDs()) {
			int count = 0;
			
			IWord word = dict.getWord(wordId);
			for (IIndexWord contextIndexWord : contextWords) {
				// int subMax = maximum number of common words in s
				// definition (gloss) and definition of any sense of w
				int subMax = 0;
				for (IWordID contextWordId : contextIndexWord.getWordIDs()) {
					IWord contextWord = dict.getWord(contextWordId);
					int sub = countCommonWords(word.getSynset().getGloss(), contextWord.getSynset().getGloss());
					if (sub > subMax) subMax = sub;
				}
				count += subMax;
			}
			
			if (maxCount < count) {
				maxCount = count;
				optimum = wordId;
			}
		}
		
		return optimum;
	}

	private int countCommonWords(String gloss1, String gloss2) {
		HashSet<String> wordsA = new HashSet<String>(Arrays.asList(gloss1.toLowerCase().split(" ")));
		HashSet<String> wordsB = new HashSet<String>(Arrays.asList(gloss2.toLowerCase().split(" ")));
		wordsA.retainAll(wordsB);
		return wordsA.size();
	}
	
	private IIndexWord taggedToIndexWord(TaggedWord taggedWord) {
		POS pos = null;
		if (taggedWord.tag().startsWith("NN")) pos = POS.NOUN;
		else if (taggedWord.tag().startsWith("JJ")) pos = POS.ADJECTIVE;
		else if (taggedWord.tag().startsWith("RB")) pos = POS.ADVERB;
		else if (taggedWord.tag().startsWith("VB")) pos = POS.VERB;
		else return null;

		// in case the word is plural, search for the stem
		List<String> stems = stemmer.findStems(taggedWord.word(), pos);
		if (!stems.isEmpty()) {
			IIndexWord indexWord = dict.getIndexWord(stems.get(0), pos);
			return indexWord;
		} else {
			return null;
		}
	}
}
