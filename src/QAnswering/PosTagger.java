package qclassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagger 
{
	private static MaxentTagger _tagger;
	
	public static List<TaggedWord> tagQuestion(String questionString) throws IOException, ClassNotFoundException
	{
		if (_tagger == null)
		{
			_tagger = new MaxentTagger("models/left3words-wsj-0-18.tagger");
			System.out.println("Tagger initilized...");
		}
		
		String[] parts = _tagger.tagTokenizedString(questionString).trim().split(" ");
		List<TaggedWord> taggedWords = new ArrayList<TaggedWord>(parts.length);
		
		for (int i = 0; i < parts.length; i++) {
			String[] tokenParts = parts[i].split("_", 2);
			taggedWords.add(new TaggedWord(tokenParts[0], tokenParts[1]));
		}
		
		return taggedWords;		
	}
}
